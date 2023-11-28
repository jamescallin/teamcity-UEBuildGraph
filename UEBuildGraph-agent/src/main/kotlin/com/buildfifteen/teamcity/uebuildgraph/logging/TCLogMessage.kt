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
import java.util.regex.Pattern

object TCLogMessage {
    fun openBlock(name: String, desc: String) : String {
        return String.format(TC_BLOCK_OPEN, escapeValue(name), escapeValue(desc))
    }

    fun closeBlock(name: String) : String {
        return String.format(TC_BLOCK_CLOSE, escapeValue(name))
    }

    fun warning(message: String) : String {
        return String.format(TC_BUILD_WARNING, escapeValue(message))
    }

    fun error(message: String, details: String) : String {
        return String.format(TC_BUILD_ERROR, escapeValue(message), escapeValue(details))
    }

    fun status(status: String, additional: String) : String {
        return String.format(TC_BUILD_STATUS, escapeValue(status), escapeValue(additional))
    }

    fun stat(key: String, value: String) : String {
        return String.format(TC_BUILD_STAT, escapeValue(key), escapeValue(value))
    }

    fun startProgress(name:String) : String {
        return String.format(TC_PROGRESS_START, escapeValue(name))
    }

    fun finishProgress(name:String) : String {
        return String.format(TC_PROGRESS_FINISH, escapeValue(name))
    }

    fun progress(message:String) : String {
        return String.format(TC_PROGRESS_MESSAGE, escapeValue(message))
    }

    fun startTestSuite(name: String) : String {
        return String.format(TC_TEST_SUITE_START, escapeValue(name))
    }

    fun finishTestSuite(name: String) : String {
        return String.format(TC_TEST_SUITE_FINISH, escapeValue(name))
    }

    fun startTest(name: String) : String {
        return String.format(TC_TEST_START, escapeValue(name), "FALSE")
    }

    fun finishTest(name: String, duration: Int = 0) : String {
        if( duration > 0)
            return String.format(TC_TEST_FINISH, escapeValue(name), duration )
        return String.format(TC_TEST_FINISH_NO_DURATION, escapeValue(name) )
    }

    fun testStdOut(name: String, out: String) : String {
        return String.format(TC_TEST_STDOUT, escapeValue(name), escapeValue(out) )
    }

    fun testStdErr(name: String, out: String) : String {
        return String.format(TC_TEST_STDERR, escapeValue(name), escapeValue(out) )
    }

    fun failTest(name: String, message: String, details: String) : String {
        return String.format(TC_TEST_FAILED, escapeValue(name), escapeValue(message), escapeValue(details) )
    }

    fun ignoreTest(name: String, message: String) : String {
        return String.format(TC_TEST_IGNORED, escapeValue(name), escapeValue(message) )
    }

    fun ugsNotification(change: String, status: BuildDataResult, project:String, badge:String) : String {
        return String.format(TC_UGS_NOTIFICATION, escapeValue(change), escapeValue(status.toString()), escapeValue(project), escapeValue(badge) )
    }

    private fun escapeValue(text: String): String {
        val matcher = ESCAPE_PATTERN.matcher(text)

        val sb = StringBuffer()
        while (matcher.find()) {
            matcher.appendReplacement(sb, tokens[matcher.group(1)])
        }

        return matcher.appendTail(sb).toString()
    }

    private const val TC_BLOCK_OPEN = "##teamcity[blockOpened name='%s' description='%s']"
    private const val TC_BLOCK_CLOSE = "##teamcity[blockClosed name='%s']"
    private const val TC_BUILD_WARNING = "##teamcity[message text='%s' status='WARNING']"
    private const val TC_BUILD_ERROR = "##teamcity[message text='%s' errorDetails='%s' status='ERROR']"
    private const val TC_BUILD_STATUS = "##teamcity[buildStatus status='%s' text='{build.status.text} %s']"
    private const val TC_BUILD_STAT = "##teamcity[buildStatisticValue key='%s' value='%s']"
    private const val TC_PROGRESS_START = "##teamcity[progressStart '%s']"
    private const val TC_PROGRESS_FINISH = "##teamcity[progressFinish '%s']"
    private const val TC_PROGRESS_MESSAGE = "##teamcity[progressMessage '%s']"
    private const val TC_TEST_SUITE_START = "##teamcity[testSuiteStarted name='%s']"
    private const val TC_TEST_SUITE_FINISH = "##teamcity[testSuiteFinished name='%s']"
    private const val TC_TEST_START = "##teamcity[testStarted name='%s' captureStandardOutput='%s']"
    private const val TC_TEST_FINISH = "##teamcity[testFinished name='%s' duration='%d']"
    private const val TC_TEST_FINISH_NO_DURATION = "##teamcity[testFinished name='%s']"
    private const val TC_TEST_STDOUT = "##teamcity[testStdOut name='%s' out='%s']"
    private const val TC_TEST_STDERR = "##teamcity[testStdErr name='%s' out='%s']"
    private const val TC_TEST_FAILED = "##teamcity[testFailed name='%s' message='%s' details='%s']"
    private const val TC_TEST_IGNORED = "##teamcity[testIgnored name='%s' message='%s']"
    private const val TC_UGS_NOTIFICATION = "##teamcity[notification notifier='UGSNotification' message='ignored' change='%s' status='%s' project='%s' badge='%s']"

    private val tokens = mapOf(
            Pair("'", "|'"),
            Pair("\n", "|n"),
            Pair("\r", "|r"),
            Pair("|", "||"),
            Pair("[", "|["),
            Pair("]", "|]"))

    private val ESCAPE_PATTERN = Pattern.compile(tokens.keys.joinToString(
            separator = "|",
            prefix = "(",
            postfix = ")",
            transform = { Pattern.quote(it) }))
}