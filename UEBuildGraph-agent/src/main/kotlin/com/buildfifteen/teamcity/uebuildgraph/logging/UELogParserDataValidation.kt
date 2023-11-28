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

class UELogParserDataValidation(mWriter: UELogWriter, private val mPathStripper: FilePathStripper) : UELogParserDefault(mWriter) {

    enum class Pending {
        NONE,
        WARNING,
        ERROR,
        CALLSTACK,
    }

    private var mPendingType = Pending.NONE
    private var mPendingMessage: StringBuilder? = null
    private var mSource: String? = null
    private var mIndent: Int = 0

    override fun onEnter(text: String, description: String) {
        mWriter.openBlock(BuildGraphConstants.BLOCK_VALIDATEASSET, description)
        mWriter.startProgress(text)
        super.onEnter(text, description)
    }

    override fun onLeave(text: String) {
        writePendingMessage()
        mWriter.finishProgress(text)
        super.onLeave(text)
        mWriter.closeBlock(BuildGraphConstants.BLOCK_VALIDATEASSET)
    }

    override fun processLine(text: String) {
        val textAddedToPending = when(mPendingType) {
            Pending.WARNING   -> processLineWarning(text)
            Pending.ERROR     -> processLineError(text)
            Pending.CALLSTACK -> processLineCallstack(text)
            else              -> false
        }

        if(!textAddedToPending) {
            val validationResult = lp_assetValidation.find(text)
            if(validationResult != null) {
                mWriter.validateAsset(text)
            }
            else {
                val matchResult = lp_errorOrWarning.find(text)
                if( matchResult != null)
                    startPendingMessage(matchResult)
                else {
                    mWriter.message(text)
                }
            }
        }
    }

    private fun processLineWarning(text: String) : Boolean {
        val matchResult = lp_multiLineEndTest.find(text)
        return if( matchResult != null) {
            val (_, message) = matchResult.destructured
            // check for special cases where we should continue appending:
            when {
                lp_scriptCallStack.containsMatchIn(message) -> {
                    val indentation = " ".repeat(mIndent)
                    addToPendingMessage("${indentation}${message}")
                    true
                }
                else -> {
                    writePendingMessage()
                    false
                }
            }
        }
        else {
            addToPendingMessage(text)
            true
        }
    }

    private fun processLineError(text: String) : Boolean {
        return if( lp_multiLineEndTest.matches(text) ) {
            writePendingMessage()
            false
        }
        else {
            addToPendingMessage(text)
            true
        }
    }

    private fun processLineCallstack(text: String) : Boolean {
        if( lp_callstackEnd.containsMatchIn(text) ) {
            addToPendingMessage(text)
            writePendingMessage()
            return true    // NB - return TRUE as don't want to process the line again
        }
        addToPendingMessage(text)
        return true
    }

    private fun startPendingMessage(matchResult: MatchResult) {
        val (whitespace, source, warnOrError, msg) = matchResult.destructured
        val assetCheckOuterResult = lp_assetCheckOuter.find(msg)
        if( source == "AssetCheck" && assetCheckOuterResult != null) {
            val (asset, failure) = assetCheckOuterResult.destructured
            val assetCheckInnerResult = lp_assetCheckInner.find(failure)
            if( assetCheckInnerResult != null) {
                val (message, testName) = assetCheckInnerResult.destructured
                mWriter.validateFail( BuildGraphMessage(BuildGraphConstants.BLOCK_VALIDATEASSET, mPathStripper.RemoveRoot(asset), "Error", testName, message, "", 0 ) )
            }
            else {
                mWriter.validateFail( BuildGraphMessage(BuildGraphConstants.BLOCK_VALIDATEASSET, mPathStripper.RemoveRoot(asset), "Error", "", failure, "", 0 ) )
            }
        }
        else {
            if(assetCheckOuterResult != null) {
                val (asset, failure) = assetCheckOuterResult.destructured
                mSource = mPathStripper.RemoveRoot(asset)
                mIndent = whitespace.length
                mPendingMessage = StringBuilder(failure)
            }
            else {
                mSource = source
                mIndent = whitespace.length
                mPendingMessage = StringBuilder(msg)
            }
            when(warnOrError.lowercase(Locale.getDefault())) {
                "warning" -> mPendingType = Pending.WARNING
                "error"   -> mPendingType = when {
                    lp_callstackStart.containsMatchIn(msg) -> Pending.CALLSTACK
                    else                                   -> Pending.ERROR
                }
                else      -> {}  // this cannot happen
            }
        }
    }

    private fun addToPendingMessage(line: String) {
        mPendingMessage?.append("\n")?.append(line)
    }

    private fun writePendingMessage() {
        // write the previous line:
        when(mPendingType) {
            Pending.WARNING -> {
                mWriter.warning( BuildGraphMessage(BuildGraphConstants.BLOCK_VALIDATEASSET, mSource!!, "Warning", "", mPendingMessage.toString(), "", mIndent ) )
                mPendingMessage = null
                mPendingType = Pending.NONE
            }
            Pending.ERROR,
            Pending.CALLSTACK -> {
                mWriter.error( BuildGraphMessage(BuildGraphConstants.BLOCK_VALIDATEASSET, mSource!!, "Error", "", mPendingMessage.toString(), "", mIndent ) )
                mPendingMessage = null
                mPendingType = Pending.NONE
            }
            else -> { }
        }
    }

    companion object {
        private val lp_errorOrWarning = Regex("^(\\s*)(.+?): (Warning|Error): (.*)")
        private val lp_multiLineEndTest = Regex("^\\s*.+?: (Display|Warning|Error|Fatal|Info): (.*)")

        private val lp_assetCheckOuter = Regex("\\[AssetLog\\] (.+)\\.u(?:map|asset): (.+)")
        private val lp_assetCheckInner = Regex("(.+) \\((.+)\\)")

        private val lp_assetValidation = Regex("^\\s*LogContentValidation: .*: Validating (.+?) .+")

        // crazy AssetCheck parsing:
        // ^(\s*)(.+?): (Warning|Error): \[AssetLog\] (.+).uasset: (?:(?:(.+) (?:\((.*)\)$))|(.*$))

        // better AssetCheck (search for parentheses on successful find):
        // ^\s*.+?: (?:Warning|Error): \[AssetLog\] (.+).uasset: (.*)

        // Special Case literals:
        // Warnings:
        private val lp_scriptCallStack = Regex("Script call stack:")

        // callstack:
        private val lp_callstackStart = Regex("begin: stack for UAT")
        private val lp_callstackEnd = Regex("end: stack for UAT")
    }
}