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

package com.buildfifteen.teamcity.uebuildgraph.fetchers

import jetbrains.buildServer.serverSide.DataItem
import org.w3c.dom.Document

class BuildGraphOptionFetcher : BuildGraphProjectDataFetcher() {

    override fun getType() = "BuildGraphOptions"

    override fun processFile(buildGraphFile: Document): List<DataItem> {
        return BuildGraphParser.parseOptions(buildGraphFile).toList().sortedBy { it.value }
    }
}