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
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser
import org.w3c.dom.Document
import java.io.FileNotFoundException
import java.nio.file.Paths

abstract class BuildGraphProjectDataFetcher : ProjectDataFetcher {

    override fun retrieveData(fsBrowser: Browser, graphFilename: String): MutableList<DataItem> {
        if( graphFilename.isNotBlank()) {
            try {
                val graphFile = fsBrowser.getElement(graphFilename)
                if (graphFile != null && graphFile.isContentAvailable ) {
                    val doc = BuildGraphParser.getDocument(graphFile.inputStream) ?: throw FileNotFoundException()
                    val includes = BuildGraphParser.parseIncludes(doc)
                    val results = arrayListOf<DataItem>()
                    val graphFolder = Paths.get(graphFilename).parent
                    for( include in includes ) {
                        // find the new file name:
                        val includedGraphFilename = graphFolder.resolve(include)
                        results += retrieveData(fsBrowser, includedGraphFilename.toString())
                    }
                    results += processFile(doc)
                    return results.toMutableList()
                }
            }
            catch (e: Exception) { }
        }
        return arrayListOf()
    }

    protected abstract fun processFile(buildGraphFile: Document): List<DataItem>
}