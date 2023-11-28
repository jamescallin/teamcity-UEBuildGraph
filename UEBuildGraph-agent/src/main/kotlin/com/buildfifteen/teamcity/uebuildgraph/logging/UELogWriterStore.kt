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
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphMessageStore

open class UELogWriterStore(protected val mStore: BuildGraphMessageStore) : UELogWriter() {

    override fun warning(message: BuildGraphMessage) {
        mStore.addMessage(message)
    }

    override fun error(message: BuildGraphMessage) {
        mStore.addMessage(message)
    }

    override fun validateFail(message: BuildGraphMessage) {
        mStore.addMessage(message)
    }
}