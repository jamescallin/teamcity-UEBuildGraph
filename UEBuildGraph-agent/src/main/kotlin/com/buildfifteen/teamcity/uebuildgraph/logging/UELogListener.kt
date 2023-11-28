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

import com.buildfifteen.teamcity.uebuildgraph.BuildGraphStatStore
import com.buildfifteen.teamcity.uebuildgraph.FilePathStripper
import jetbrains.buildServer.agent.runner.ProcessListenerAdapter
import java.io.File
import java.util.*

class UELogListener(
    private val mWriter: UELogWriter,
    pathStripper: FilePathStripper,
    statStore: BuildGraphStatStore?,
    private val mGauntletCommand: String = "RunUnrealTests") : ProcessListenerAdapter() {

    enum class State {
        DEFAULT,
        UBT,
        TASK,
        COMPILER,
        COMMANDLET,
        COOKSTATS,
        GAUNTLET,
        VALIDATEDATA,
    }

    private var mCurrentState = State.DEFAULT

    private var mActiveParser: UELogParser

    private val mCompilerParser = UELogParserCompiler(mWriter, pathStripper)
    private val mCommandletParser = UELogParserCommandlet(mWriter, statStore, true, pathStripper)
    private val mCookStatsParser = UELogParserCookStats(mWriter, statStore)
    private val mDefaultParser = UELogParserDefault(mWriter)
    private val mToolParser = UELogParserTool(mWriter)
    private val mTaskParser = UELogParserTask(mWriter)
    private val mGauntletParser = UELogParserGauntlet(mWriter)
    private val mDataValidationParser = UELogParserDataValidation(mWriter, pathStripper)

    private var mGraphNodeName: String? = null

    init {
        mActiveParser = mDefaultParser
    }

    private fun isLineRunOrBuild(line: String) : Boolean {
        lp_taskStart.find(line)?.let { matchResult ->
            val (exeName, parameters) = matchResult.destructured
            var nextState = State.COMMANDLET
            var description = exeName

            if(exeName.lowercase(Locale.getDefault()).endsWith("editor-cmd")) {
                description = "Editor-cmd"
                lp_commandletName.find(parameters)?.let {
                    val (cmdName) = it.destructured
                    description = "Commandlet: $cmdName"
                    nextState = when(cmdName) {
                        "DataValidation" -> State.VALIDATEDATA
                        else -> nextState
                    }
                }
            }
            else if(exeName.lowercase(Locale.getDefault()).endsWith("dotnet") ) {
                lp_dotnetLoadedDLL.find(parameters)?.let { matchResult2 ->
                    val (dllName, dllParams) = matchResult2.destructured

                    // in the parameters, replace any text in quotes with a blank string (to remove them)
                    val nonQuotedParams = lp_quotedSection.replace(dllParams, "")
                    // now, break in to a list based on spaces as delimiters, remove the entries that start with
                    // '-' (ie are switches in teh command) and then recombine in to a string again
                    val params = nonQuotedParams.trim().splitToSequence(' ')
                        .filter { it.isNotBlank() && !it.startsWith("-") }
                        .toList().joinToString(" ")

                    description = "$params (${dllName})"
                    nextState = when(dllName) {
                        "UnrealBuildTool" -> State.UBT
                        "AutomationTool"  -> {
                                                if(params.startsWith(mGauntletCommand, true) )
                                                    State.GAUNTLET
                                                else
                                                    State.COMMANDLET
                                             }
                        else              -> nextState
                    }

                }
            }
            else {
                nextState = State.TASK
            }
            setState(nextState, description, line)
            return true
        }

        lp_CompilerStart.find(line)?.let {
            setState(State.COMPILER, line, "")
            return true
        }
        lp_CompilerStart2.find(line)?.let {
            setState(State.COMPILER, line, "")
            return true
        }

        return false
    }

    private fun isLineTook(line: String) : Boolean {
        lp_taskEnd.find(line)?.let {
            if( (mCurrentState == State.TASK) || (mCurrentState == State.UBT)) {
                val (timeTaken, exeName) = it.destructured
                mWriter.message(line)
                mWriter.stat(exeName, timeTaken)
                mWriter.closeBlock(exeName)
            }
            else
                setState(State.DEFAULT, "", line, false)
            return true
        }
        return false
    }

    private fun isLineCookStats(line: String) : Boolean {
        lp_cookEndMarker.find(line)?.let {
            setState(State.COOKSTATS, "", line)
            return true
        }
        return false
    }

    private fun isWarningAndErrorSummary(line: String) : Boolean {
        lp_warningAndErrorSummary.find(line)?.let {
            setState(State.DEFAULT, "", line, true)
            return true
        }
        return false
    }

    private fun runStateDefault(line: String) {
        if( isLineRunOrBuild(line) ) return
        mActiveParser.processLine(line)
    }

    private fun runStateTool(line: String) {
        if( isLineTook(line)) return
        if( isLineRunOrBuild(line) ) return
        mActiveParser.processLine(line)
    }

    private fun runStateCompile(line: String) {
        if( isLineRunOrBuild(line) ) return
        mActiveParser.processLine(line)
    }

    private fun runStateCommandlet(line: String) {
        if( isLineTook(line)) return
        if( isLineCookStats(line)) return
        if( isLineRunOrBuild(line) ) return
        mActiveParser.processLine(line)
    }

    private fun runStateGauntlet(line: String) {
        if( isLineTook(line)) return
        mActiveParser.processLine(line)
    }

    private fun runStateValidateData(line: String) {
        if( isLineTook(line)) return
        if( isWarningAndErrorSummary(line) ) return
        mActiveParser.processLine(line)
    }

    override fun onStandardOutput(line: String) {
        if (line.isBlank()) return

        lp_buildGraphNode.find(line)?.let {
            val (stepNum, totalSteps, stepName) = it.destructured
            val description = String.format("Node %s (of %s)", stepNum, totalSteps)
            changeGraphNode(description, stepName)
            mCurrentState = State.DEFAULT
            return
        }

        when(mCurrentState) {
            State.DEFAULT      -> runStateDefault(line)
            State.TASK         -> runStateTool(line)         // NB, intentionally Tool - it's the same
            State.UBT          -> runStateTool(line)
            State.COMPILER     -> runStateCompile(line)
            State.COMMANDLET   -> runStateCommandlet(line)
            State.COOKSTATS    -> runStateTool(line)         // NB, intentionally Tool - it's the same
            State.GAUNTLET     -> runStateGauntlet(line)
            State.VALIDATEDATA -> runStateValidateData(line)
        }
    }

    //todo: Properly parse any errors as well:
    // override fun onErrorOutput(text: String)

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        mWriter.processStarted(programCommandLine)
    }

    override fun processFinished(exitCode: Int) {
        mWriter.processFinished(exitCode)
    }

    private fun setState(newState: State, description: String, text: String, textIsForNewState: Boolean = true) {
        if( (newState == State.DEFAULT) && (mCurrentState == State.DEFAULT) ) {
            mActiveParser.processLine(text)
            return
        }
        if( newState != State.TASK ) {
            when (textIsForNewState) {
                true -> mActiveParser.onLeave()
                false -> mActiveParser.onLeave(text)
            }
        }
        else {
            mTaskParser.mPreviousState = mCurrentState;
        }
        var switchToState = newState
        if( mCurrentState == State.TASK && newState == State.DEFAULT ) {
            switchToState = mTaskParser.mPreviousState
            mTaskParser.mPreviousState = State.DEFAULT
        }
        mActiveParser = when(switchToState) {
            State.DEFAULT      -> mDefaultParser
            State.TASK         -> mTaskParser
            State.UBT          -> mToolParser
            State.COMPILER     -> mCompilerParser
            State.COMMANDLET      -> mCommandletParser
            State.COOKSTATS    -> mCookStatsParser
            State.GAUNTLET     -> mGauntletParser
            State.VALIDATEDATA -> mDataValidationParser
        }
        when(textIsForNewState) {
            true  -> mActiveParser.onEnter(text, description)
            false -> mActiveParser.onEnter("", description)
        }
        mCurrentState = newState
    }

    private fun changeGraphNode(name: String, description: String) {
        if(mGraphNodeName != null ) {
            mActiveParser.onLeave()
            mActiveParser = mDefaultParser
            mWriter.closeBlock( mGraphNodeName!! )
        }
        mCurrentState = State.DEFAULT
        mWriter.openBlock(name, description )
        mGraphNodeName = name
    }

    companion object {
        private val lp_taskStart = Regex( "^\\s*Running: .*\\\\(.+)\\.[Ee][Xx][Ee] (.+)" )
        private val lp_taskEnd = Regex("^\\s*Took ([\\d\\.]+)s to run (.+), ExitCode=(\\d+)")

        private val lp_dotnetLoadedDLL = Regex("\".*\\\\(.*?)\\.dll\".(.*)")

        private val lp_cookEndMarker = Regex("LogCook: Display: Finishing up...")
        private val lp_warningAndErrorSummary = Regex("LogInit: Display: Warning/Error Summary \\(Unique only\\)")

        private val lp_CompilerStart = Regex("^Building \\d+ actions with \\d+ processes")
        private val lp_CompilerStart2 = Regex("^------ Building \\d* action\\(s\\) started ------")

        private val lp_buildGraphNode = Regex("\\*\\*\\*\\*\\*\\* \\[(\\d+)/(\\d+)\\] (.*)")

        // find all text in quotes:
        private val lp_quotedSection = Regex("\"[\\S\\s]*?\"")
        private val lp_commandletName = Regex( "-run=(\\S*)")
    }
}