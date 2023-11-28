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

open class UELogWriter {
    open fun processStarted(programCommandLine: String) {}
    open fun processFinished(exitCode: Int) {}

    open fun openBlock(name: String, desc: String) {}
    open fun closeBlock(name: String) {}

    open fun message(message: String) {}
    open fun warning(message: BuildGraphMessage) {}
    open fun error(message: BuildGraphMessage) {}

    open fun startProgress(name: String) {}
    open fun finishProgress(name: String) {}
    open fun progress(message: String) {}

    open fun status(status: String, additional: String) {}

    open fun stat(key: String, value: String) {}

    open fun startTestSuite(name: String) {}
    open fun finishTestSuite(name: String) {}
    open fun startTest(name: String) {}
    open fun finishTest(name: String) {}

    open fun testStdOut(name: String, out: String) {}
    open fun testStdErr(name: String, out: String) {}
    open fun failTest(name: String, message: String, details: String) {}
    open fun ignoreTest(name: String, message: String) {}

    open fun validateAsset(message: String) {}
    open fun validateFail(message: BuildGraphMessage) {}
}
