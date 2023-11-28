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

class UELogWriters : UELogWriter() {
    private val mWriters = mutableListOf<UELogWriter>()

    fun addWriter(writer: UELogWriter) {
        mWriters.add(writer)
    }

    override fun processStarted(programCommandLine: String) {
        for( writer in mWriters )
            writer.processStarted(programCommandLine)
    }

    override fun processFinished(exitCode: Int) {
        for( writer in mWriters )
            writer.processFinished(exitCode)
    }

    override fun openBlock(name: String, desc: String) {
        for( writer in mWriters )
            writer.openBlock(name, desc)
    }

    override fun closeBlock(name: String) {
        for( writer in mWriters )
            writer.closeBlock(name)
    }

    override fun message(message: String) {
        for( writer in mWriters )
            writer.message(message)
    }

    override fun warning(message: BuildGraphMessage) {
        for( writer in mWriters )
            writer.warning(message)
    }

    override fun error(message: BuildGraphMessage) {
        for( writer in mWriters )
            writer.error(message)
    }

    override fun status(status: String, additional: String) {
        for( writer in mWriters )
            writer.status(status, additional)
    }

    override fun stat(key: String, value: String) {
        for( writer in mWriters )
            writer.stat(key, value)
    }

    override fun startProgress(name: String) {
        for( writer in mWriters )
            writer.startProgress(name)
    }

    override fun finishProgress(name: String) {
        for( writer in mWriters )
            writer.finishProgress(name)
    }

    override fun progress(message: String) {
        for( writer in mWriters )
            writer.progress(message)
    }

    override fun startTestSuite(name: String) {
        for( writer in mWriters)
            writer.startTestSuite(name)
    }

    override fun finishTestSuite(name: String)  {
        for( writer in mWriters)
            writer.finishTestSuite(name)
    }

    override fun startTest(name: String){
        for( writer in mWriters)
            writer.startTest(name)
    }

    override fun finishTest(name: String)  {
        for( writer in mWriters)
            writer.finishTest(name)
    }

    override fun testStdOut(name: String, out: String) {
        for( writer in mWriters)
            writer.testStdOut(name, out)
    }

    override fun testStdErr(name: String, out: String) {
        for( writer in mWriters)
            writer.testStdErr(name, out)
    }

    override fun failTest(name: String, message: String, details: String) {
        for( writer in mWriters)
            writer.failTest(name, message, details)
    }

    override fun ignoreTest(name: String, message: String) {
        for (writer in mWriters)
            writer.ignoreTest(name, message)
    }

    override fun validateAsset(message: String) {
        for( writer in mWriters)
            writer.validateAsset(message)
    }

    override fun validateFail(message: BuildGraphMessage) {
        for( writer in mWriters)
            writer.validateFail(message)
    }
}