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

open class UELogParserDefault(protected val mWriter: UELogWriter) : UELogParser {

    override fun onEnter(text: String, description: String) {
        if(text.isNotBlank())
            mWriter.message(text)
    }

    override fun processLine(text: String) {
        mWriter.message(text)
    }

    override fun processError(text: String) {
        mWriter.error(BuildGraphMessage( BuildGraphConstants.BLOCK_DEFAULT,"", "error", "", text, "", 0))
    }

    override fun onLeave(text: String) {
        if(text.isNotBlank())
            mWriter.message(text)
    }
}