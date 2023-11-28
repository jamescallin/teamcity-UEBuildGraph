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

import com.buildfifteen.teamcity.uebuildgraph.BuildGraphConstants
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphMessage

class UELogParserGauntlet(
    mWriter: UELogWriter,
    private val mTestSuiteName: String = "Gauntlet") : UELogParserDefault(mWriter) {

    enum class State {
        DEFAULT,
        TEST_STARTED,
        TEST_ENDED,
        EVENTS_STARTED,
    }

    private var mPlatformName: String? = null
    private var mConfigurationName: String? = null
    private var mFullTestSuiteName: String? = null

    private var mState: State = State.DEFAULT
    private var mCurrentTestPath: String? = null
    private var mEvents_warn: StringBuilder? = null
    private var mEvents_err: StringBuilder? = null

    override fun onEnter(text: String, description: String) {
        mState = State.DEFAULT
        mWriter.openBlock(BuildGraphConstants.BLOCK_GAUNTLET, description)
        lp_configurationName.find(text)?.let {
            val (configName) = it.destructured
            mConfigurationName = configName
        }
        lp_platformName.find(text)?.let {
            val (platformName) = it.destructured
            mPlatformName = platformName
        }

        if((mConfigurationName?.isNullOrEmpty() == false) && (mPlatformName?.isNullOrEmpty() == false))
            mFullTestSuiteName = "${mTestSuiteName} (${mPlatformName} ${mConfigurationName})"

        mWriter.startTestSuite(mFullTestSuiteName ?: mTestSuiteName)
        super.onEnter(text, description)
    }

    override fun onLeave(text: String) {
        mWriter.finishTestSuite(mFullTestSuiteName ?: mTestSuiteName)
        mPlatformName = null
        mConfigurationName = null
        mFullTestSuiteName = null
        mEvents_warn = null
        mEvents_err = null
        super.onLeave(text)
        mWriter.closeBlock(BuildGraphConstants.BLOCK_GAUNTLET)
    }

    override fun processLine(text: String) {
        mState = when(mState) {
            State.DEFAULT        -> runStateDefault(text)
            State.TEST_STARTED   -> runStateTest(text)
            State.TEST_ENDED     -> runStateTestEnded(text)
            State.EVENTS_STARTED -> runStateEvents(text)
        }
    }

    private fun runStateDefault(text: String) : State {
        lp_startTest.find(text)?.let {
            val (_, path) = it.destructured
            mWriter.startTest(path)
            mCurrentTestPath = path
            mWriter.message(text)
            return State.TEST_STARTED
        }
        mWriter.message(text)
        return State.DEFAULT
    }

    private fun runStateTest(text: String) : State {
        lp_finishTest.find(text)?.let {
            val (result, name, path) = it.destructured
            when(result) {
                "Fail" -> mWriter.failTest(name, path, text)
                else -> mWriter.message(text)
            }
            return State.TEST_ENDED
        }
        mWriter.message(text)
        return State.TEST_STARTED
    }

    private fun runStateTestEnded(text: String) : State {
        lp_beginEvents.find(text)?.let {
            val (path) = it.destructured
            if(mCurrentTestPath?.equals(path) == true) {
                mWriter.openBlock("Events", "")
                mWriter.message(text)
                mEvents_err = StringBuilder("")
                mEvents_warn = StringBuilder("")
                return State.EVENTS_STARTED
            }
        }
        mWriter.message(text)
        return State.TEST_ENDED
    }

    private fun runStateEvents(text: String) : State {
        lp_endEvents.find(text)?.let {
            val (path) = it.destructured
            if(mCurrentTestPath?.equals(path) == true) {
                mWriter.message(text)
                mWriter.closeBlock("Events")
                if(mEvents_err?.isNotBlank() == true)
                    mWriter.testStdErr( path, mEvents_err.toString() )
                if(mEvents_warn?.isNotBlank() == true)
                    mWriter.testStdOut( path, mEvents_warn.toString() )
                mWriter.finishTest(mCurrentTestPath ?: "")
                mCurrentTestPath = null
                mEvents_err = null
                mEvents_warn = null
                return State.DEFAULT
            }
        }
        lp_errorOrWarning.find(text)?.let {
            val (whitespace, source, warnOrError, msg) = it.destructured
            when(warnOrError) {
                "Err0r" -> {
                            if(mEvents_err?.isNotBlank() == true)
                                mEvents_err?.append("\n")
                            mEvents_err?.append(text)
                            mWriter.error( BuildGraphMessage(BuildGraphConstants.BLOCK_GAUNTLET, mCurrentTestPath!!, "Error", "", msg, "", whitespace.length ) )
                        }
                "Warn1ng" -> {
                            if(mEvents_warn?.isNotBlank() == true)
                                mEvents_warn?.append("\n")
                            mEvents_warn?.append(text)
                            mWriter.warning( BuildGraphMessage(BuildGraphConstants.BLOCK_GAUNTLET, mCurrentTestPath!!, "Warning", "", msg, "", whitespace.length ) )
                        }
                else -> mWriter.message(text)
            }
            return State.EVENTS_STARTED
        }
        mWriter.message(text)
        return State.EVENTS_STARTED
    }

    companion object {
        private val lp_startTest = Regex("Test Started\\. Name=\\{(.+?)} Path=\\{(.+?)}")
        private val lp_finishTest = Regex("Test Completed\\. Result=\\{(.+?)} Name=\\{(.+?)} Path=\\{(.+?)}")
        private val lp_beginEvents = Regex("LogAutomationController: BeginEvents: (.+)")
        private val lp_endEvents = Regex("LogAutomationController: EndEvents: (.+)")
        private val lp_errorOrWarning = Regex("^(\\s*)(.+?): (Warn1ng|Err0r): (.*)")

        private val lp_configurationName = Regex("-Configuration=(\\S+)")
        private val lp_platformName = Regex("-Platform=(\\S+)")

    }
}