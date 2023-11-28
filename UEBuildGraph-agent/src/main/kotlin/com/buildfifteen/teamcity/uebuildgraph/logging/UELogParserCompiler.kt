//  _           _ _     _  __ _  __ _
// | |__  _   _(_) | __| |/ _(_)/ _| |_ ___  ___ _ __
// | '_ \| | | | | |/ _` | |_| | |_| __/ _ \/ _ \ '_ \
// | |_) | |_| | | | (_| |  _| |  _| ||  __/  __/ | | |
// |_.__/ \__,_|_|_|\__,_|_| |_|_|  \__\___|\___|_| |_|
//
// ----------------------------------------------------------------------------
// Copyright (c) James Callin 2020-2023
// Licensed under the MIT license.
// See LICENSE.TXT in the project root for license information.
// ----------------------------------------------------------------------------

package com.buildfifteen.teamcity.uebuildgraph.logging

import com.buildfifteen.teamcity.uebuildgraph.BuildGraphMessage
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphConstants
import com.buildfifteen.teamcity.uebuildgraph.FilePathStripper
import java.util.*

class UELogParserCompiler(mWriter: UELogWriter, private val mPathStripper: FilePathStripper) : UELogParserDefault(mWriter) {

    enum class Pending {
        NONE,
        INTRO,
        WARNING,
        ERROR,
    }

    private var mPendingType = Pending.NONE
    private var mPendingMessage: StringBuilder? = null
    private var mIntroMessage: StringBuilder? = null
    private var mStoredLine: String? = null
    private var mLookahead: Boolean = false
    private var mSource: String? = null
    private var mCode: String? = null


    override fun onEnter(text: String, description: String) {
        mWriter.openBlock(BuildGraphConstants.BLOCK_COMPILER, description)
        super.onEnter(text, description)
    }

    override fun onLeave(text: String) {
        super.onLeave(text)
        mWriter.closeBlock(BuildGraphConstants.BLOCK_COMPILER)
    }

    override fun processLine(text: String) {

        var textAddedToPending = false
        // if this is an error line then we need to reset all the possible other things,
        // finish the pending ops, and start again:
        val matchResult = lp_errorOrWarning.find(text)
        if((mPendingType != Pending.INTRO) && matchResult != null) {
            writePendingMessage()
        } else {
            textAddedToPending = when (mPendingType) {
                Pending.NONE -> false
                else -> processLinePending(text)
            }
        }

        if(!textAddedToPending) {
            if( matchResult != null)
                addErrorToPendingMessage(matchResult)
            else {
                if(text.startsWith("In file included from")) {
                    storeIntroMessage(text)
                }
                else {
                    mWriter.message(text)
                }
            }
        }
    }

    private fun processLinePending(text: String) : Boolean {
        // if we had an error/warning then we might have the line it was at printed
        // we look for that by seeing if the first non-whitespace in the following line
        // is a caret:
        if(mLookahead) {
            // if this is a continuation line, we should just add it, and stop looking
            // if it's a note then we should add it, and continue looking for continuation lines
            // otherwise, remember this line but don't add it yet and stop looking ahead
            val matchResult = lp_continuation.find(text)
            if( matchResult != null ) {
                addContinuationMessage(text, matchResult)
            }
            else {
                mStoredLine = text
                mLookahead = false
            }
            return true
        }
        if(mStoredLine != null) {
            // ok, we have a stored line we might add.  Does this line start with a caret?
            // If so, we need to add the stored line and this one to the Pending message
            //  and return TRUE as we've dealt with everything.
            // if not, we write the Pending message (and terminate it) and write this line
            //  and return FALSE as this line might be a new error/warning we want to deal with
            return if(lp_caretFirst.containsMatchIn(text)) {
                addToPendingMessage(mStoredLine!!)
                mStoredLine = null
                addToPendingMessage(text)
                true
            } else {
                writePendingMessage()
                mWriter.message(mStoredLine!!)
                mStoredLine = null
                false
            }
        }
        // finally, if we started with a 'In File Included From', this line could be the actual error:
        if(mPendingType == Pending.INTRO) {
            val matchResult = lp_errorOrWarning.find(text)
            if( matchResult != null) {
                addErrorToPendingMessage(matchResult)
                return true
            } else {
                if(text.startsWith("In file included from")) {
                    storeIntroMessage(text)
                    return true
                }
            }
        }

        // Otherwise: if this line is a Note, we should append it.
        // similarly, if the line starts with eight spaces we should append that:
        val matchResult = lp_continuation.find(text)
        return if( matchResult != null ) {
            addContinuationMessage(text, matchResult)
            true
        }
        else {
            writePendingMessage()
            false
        }
    }

    private fun addContinuationMessage(text: String, matchResult: MatchResult) {
        val (source, msg) = matchResult.destructured
        if(source.isNotBlank() && msg.isNotBlank()) {
            val relativeSource = mPathStripper.RemoveRoot(source)
            addToPendingMessage("${relativeSource}${msg}")
            mLookahead = true
        }
        else{
            addToPendingMessage(text)
            mLookahead = false
        }
    }

    private fun storeIntroMessage(text: String) {
        if(mIntroMessage == null)
            mIntroMessage = StringBuilder(text)
        else
            mIntroMessage?.append("\n")?.append(text)
        mPendingType = Pending.INTRO
    }

    private fun addErrorToPendingMessage(matchResult: MatchResult) {
        val (_, source, warnOrError, code, msg) = matchResult.destructured
        mSource = mPathStripper.RemoveRoot(source)
        mCode = code
        if(mPendingMessage == null )
            mPendingMessage = StringBuilder(msg)
        else
            mPendingMessage?.append("\n")?.append(msg)
        when(warnOrError.lowercase(Locale.getDefault())) {
            "warning" -> mPendingType = Pending.WARNING
            "error"   -> mPendingType = Pending.ERROR
            else      -> {}  // this cannot happen
        }
        mLookahead = true
    }

    private fun addToPendingMessage(line: String) {
        mPendingMessage?.append("\n")?.append(line)
    }

    private fun writePendingMessage() {
        // write the previous line:
        when(mPendingType) {
            Pending.WARNING -> {
                mWriter.warning(
                    BuildGraphMessage(
                        BuildGraphConstants.BLOCK_COMPILER,
                        mSource!!,
                        "Warning",
                        mCode!!,
                        mPendingMessage.toString(),
                        if(mIntroMessage != null) mIntroMessage.toString() else "",
                        0
                    )
                )
            }
            Pending.ERROR -> {
                mWriter.error(
                    BuildGraphMessage(
                        BuildGraphConstants.BLOCK_COMPILER,
                        mSource!!,
                        "Error",
                        mCode!!,
                        mPendingMessage.toString(),
                        if(mIntroMessage != null) mIntroMessage.toString() else "",
                        0
                    )
                )
            }
            Pending.INTRO -> {
                // this is bad, but not sure how to handle that
                mWriter.message( mIntroMessage.toString())
                mWriter.message( mPendingMessage.toString())
            }
            else -> { }
        }
        mPendingMessage = null
        mCode = null
        mPendingType = Pending.NONE
        mIntroMessage = null
        mLookahead = false
        mSource = null
    }


    companion object {
        private val lp_errorOrWarning = Regex("^(\\s*)(.*): (warning|error|fatal error):? (?:(.+?): )?(.*)")
        private val lp_continuation = Regex("^(?:        |\\t\\t|(.*)(: note:.*))")
        private val lp_caretFirst = Regex("^\\s*\\^")
    }
}