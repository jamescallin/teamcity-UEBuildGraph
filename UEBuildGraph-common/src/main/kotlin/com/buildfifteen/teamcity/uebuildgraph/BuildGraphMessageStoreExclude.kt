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
package com.buildfifteen.teamcity.uebuildgraph

class BuildGraphMessageStoreExclude(private val excludeBlocks: Array<String>) : BuildGraphMessageStore() {
    override fun addMessage(message: BuildGraphMessage): Boolean {
        if( message.block in excludeBlocks )
            return false
        return super.addMessage(message)
    }
}