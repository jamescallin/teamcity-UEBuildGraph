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
import jetbrains.buildServer.agent.BuildProgressLogger

class UELogWriterDefault(private val mLogger: BuildProgressLogger) : UELogWriter() {

    override fun message(message: String) {
        mLogger.message(message)
    }

    override fun warning(message: BuildGraphMessage) {
        mLogger.message(message.toString())
    }

    override fun error(message: BuildGraphMessage) {
        mLogger.message(message.toString())
    }
}
