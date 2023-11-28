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
import com.buildfifteen.teamcity.uebuildgraph.logging.UELogListener

class UELogParserTask(mWriter: UELogWriter) : UELogParserDefault(mWriter) {
    var mPreviousState: UELogListener.State = UELogListener.State.DEFAULT

    override fun onEnter(text: String, description: String ) {
        mWriter.openBlock(BuildGraphConstants.BLOCK_TASK, description)
        super.onEnter(text, description)
    }

    override fun onLeave(text: String) {
        super.onLeave(text)
        mWriter.closeBlock(BuildGraphConstants.BLOCK_TASK)
    }
}
