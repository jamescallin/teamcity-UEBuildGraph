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

import com.buildfifteen.teamcity.uebuildgraph.BuildDataResult
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphMessage
import com.buildfifteen.teamcity.uebuildgraph.UGSInfo
import jetbrains.buildServer.agent.BuildProgressLogger

class UELogWriterTeamCity(
    private val mLogger: BuildProgressLogger,
    private val mUGSInfo: UGSInfo? = null,
    private val mSendUGSNotifications: Boolean = false,
    private val mSuppressStartNotification: Boolean = false
    ) : UELogWriter()
{
    var mBuildSuccessful = true;

    override fun processStarted(programCommandLine: String) {
        if( mUGSInfo != null && mSendUGSNotifications && !mSuppressStartNotification )
            mLogger.message( TCLogMessage.ugsNotification(mUGSInfo.changeNumber, BuildDataResult.Starting, mUGSInfo.project, mUGSInfo.badge) )
    }

    override fun processFinished(exitCode: Int) {
        if( mUGSInfo != null && mSendUGSNotifications ) {
            if( exitCode == 0 && mBuildSuccessful ) {
                mLogger.message( TCLogMessage.ugsNotification(mUGSInfo.changeNumber, BuildDataResult.Success, mUGSInfo.project, mUGSInfo.badge) )
            }
            else {
                mLogger.message( TCLogMessage.ugsNotification(mUGSInfo.changeNumber, BuildDataResult.Failure, mUGSInfo.project, mUGSInfo.badge) )
            }
        }
    }

    override fun openBlock(name: String, desc: String) {
        mLogger.message( TCLogMessage.openBlock(name, desc) )
    }

    override fun closeBlock(name: String) {
        mLogger.message( TCLogMessage.closeBlock(name))
    }

    override fun message(message: String) {
        mLogger.message(message)
    }

    override fun warning(message: BuildGraphMessage) {
        mLogger.message(TCLogMessage.warning(message.toString()))
    }

    override fun error(message: BuildGraphMessage) {
        mLogger.message(TCLogMessage.error(message.code, message.toString()))
        mBuildSuccessful = false
    }

    override fun status(status: String, additional: String) {
        mLogger.message(TCLogMessage.status( status, additional))
    }

    override fun stat(key: String, value: String) {
        mLogger.message(TCLogMessage.stat(key, value))
    }

    override fun startProgress(name: String) {
        mLogger.progressStarted(name)
    }

    override fun finishProgress(name: String) {
        mLogger.progressFinished()
    }

    override fun progress(message: String) {
        mLogger.progressMessage(message)
    }

    override fun startTestSuite(name: String) {
        mLogger.message(TCLogMessage.startTestSuite(name))
    }
    override fun finishTestSuite(name: String) {
        mLogger.message(TCLogMessage.finishTestSuite(name))
    }
    override fun startTest(name: String) {
        mLogger.message(TCLogMessage.startTest(name))
    }
    override fun finishTest(name: String) {
        mLogger.message(TCLogMessage.finishTest(name))
    }

    override fun testStdOut(name: String, out: String) {
        mLogger.message(TCLogMessage.testStdOut(name, out))
    }

    override fun testStdErr(name: String, out: String) {
        mLogger.message(TCLogMessage.testStdErr(name, out))
    }

    override fun failTest(name: String, message: String, details: String) {
        mLogger.message(TCLogMessage.failTest(name, message, details))
        mBuildSuccessful = false
    }

    override fun ignoreTest(name: String, message: String) {
        mLogger.message(TCLogMessage.ignoreTest(name, message))
    }

    override fun validateAsset(message: String) {
        mLogger.message(message)
    }

    override fun validateFail(message: BuildGraphMessage) {
        mLogger.message(TCLogMessage.error(message.code, message.toString()))
        mBuildSuccessful = false
    }
}
