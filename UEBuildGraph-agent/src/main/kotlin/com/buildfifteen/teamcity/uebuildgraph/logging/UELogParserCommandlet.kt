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
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphStatStore
import com.buildfifteen.teamcity.uebuildgraph.FilePathStripper
import java.util.*

class UELogParserCommandlet(
    mWriter: UELogWriter,
    private val mBuildGraphStatsStore: BuildGraphStatStore? = null,
    private val mUpdateProgress: Boolean,
    private val mPathStripper: FilePathStripper
) : UELogParserDefault(mWriter) {

    enum class Pending {
        NONE,
        WARNING,
        ERROR,
        CALLSTACK,
        ASSETCHECK,
    }

    private var mPendingType = Pending.NONE
    private var mPendingMessage: StringBuilder? = null
    private var mSource: String? = null
    private var mIndent: Int = 0
    private var mBlockName: String = BuildGraphConstants.BLOCK_CONTENT

    private val mProcessStats: Boolean
        get() = mBuildGraphStatsStore!=null || mUpdateProgress

    override fun onEnter(text: String, description: String) {
        mWriter.openBlock(BuildGraphConstants.BLOCK_CONTENT, description)
        mWriter.startProgress(text)
        super.onEnter(text, description)
    }

    override fun onLeave(text: String) {
        writePendingMessage()
        mWriter.finishProgress(text)
        super.onLeave(text)
        mWriter.closeBlock(BuildGraphConstants.BLOCK_CONTENT)
    }

    override fun processLine(text: String) {

        val textAddedToPending = when(mPendingType) {
            Pending.WARNING   -> processLineWarning(text)
            Pending.ERROR     -> processLineError(text)
            Pending.CALLSTACK -> processLineCallstack(text)
            else              -> false
        }

        if(!textAddedToPending) {
            val matchResult = lp_errorOrWarning.find(text)
            if( matchResult != null)
                startPendingMessage(matchResult)
            else {
                val lineWritten = mProcessStats && processLineForStats(text)
                if( !lineWritten )
                    mWriter.message(text)
            }
        }
    }

    private fun processLineForStats(text: String): Boolean {
        lp_cookProgress.find(text)?.let {
            val (done, remaining, total) = it.destructured
            val time = System.currentTimeMillis()
            mBuildGraphStatsStore?.addSequencedStat("Cooker_Cooked", time, done)
            mBuildGraphStatsStore?.addSequencedStat("Cooker_Remain", time, remaining)
            mBuildGraphStatsStore?.addSequencedStat("Cooker_Total", time, total)
            if(mUpdateProgress) {
                mWriter.progress(String.format("Cooked: %s Remaining: %s (Total: %s)", done, remaining, total))
                return true
            }
            return false
        }
        lp_cookDiagnosticTest.find(text)?.let { diagStats ->
            val (stats) = diagStats.destructured
            val time = System.currentTimeMillis()
            lp_cookDiagnosticStat.findAll(stats).forEach {
                val (stat, value) = it.destructured
                mBuildGraphStatsStore?.addSequencedStat(stat, time, value)
            }
        }
        return false
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
        if(assetCheckOuterResult != null) {
            val (asset, failure) = assetCheckOuterResult.destructured
            mSource = mPathStripper.RemoveRoot(asset)
            mPendingMessage = StringBuilder(failure)
            mBlockName = BuildGraphConstants.BLOCK_VALIDATEASSET
        }
        else {
            mSource = source
            mPendingMessage = StringBuilder(msg)
            mBlockName = BuildGraphConstants.BLOCK_CONTENT
        }
        mIndent = whitespace.length
        when(warnOrError.lowercase(Locale.getDefault())) {
            "warning" -> mPendingType = Pending.WARNING
            "error"   -> mPendingType = when {
                    lp_callstackStart.containsMatchIn(msg) -> Pending.CALLSTACK
                    else                                   -> Pending.ERROR
                }
            else      -> {}  // this cannot happen
        }
    }

    private fun addToPendingMessage(line: String) {
        mPendingMessage?.append("\n")?.append(line)
    }

    private fun writePendingMessage() {
        // write the previous line:
        when(mPendingType) {
            Pending.WARNING -> {
                mWriter.warning( BuildGraphMessage(mBlockName, mSource!!, "Warning", "", mPendingMessage.toString(), "", mIndent ) )
                mPendingMessage = null
                mPendingType = Pending.NONE
            }
            Pending.ERROR,
            Pending.CALLSTACK -> {
                mWriter.error( BuildGraphMessage(mBlockName, mSource!!, "Error", "", mPendingMessage.toString(), "", mIndent ) )
                mPendingMessage = null
                mPendingType = Pending.NONE
            }
            else -> { }
        }
    }

    companion object {
        private val lp_errorOrWarning = Regex("^(\\s*)(.+?): (Warning|Error): (.*)")
        private val lp_multiLineEndTest = Regex("^\\s*.+?: (Display|Warning|Error|Fatal|Info): (.*)")
        private val lp_cookDiagnosticTest = Regex("^\\s*.+?: Display: Cook Diagnostics:(.*)")
        private val lp_cookDiagnosticStat = Regex("(\\w+)=(\\d+)")
        private val lp_cookProgress = Regex("^.+?: Display: Cooked packages (\\d+) Packages Remain (\\d+) Total (\\d+)")

        private val lp_assetCheckOuter = Regex("\\[AssetLog\\] (.+)\\.u(?:map|asset): (.+)")

        // crazy AssetCheck parsing:
        // ^(\s*)(.+?): (Warning|Error): \[AssetLog\] (.+).uasset: (?:(?:(.+) (?:\((.*)\)$))|(.*$))


        // Special Case literals:
        // Warnings:
        private val lp_scriptCallStack = Regex("Script call stack:")

        // callstack:
        private val lp_callstackStart = Regex("begin: stack for UAT")
        private val lp_callstackEnd = Regex("end: stack for UAT")
    }
}