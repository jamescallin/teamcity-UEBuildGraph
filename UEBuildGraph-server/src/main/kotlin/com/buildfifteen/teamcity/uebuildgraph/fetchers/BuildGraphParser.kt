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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.DataItem
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.InputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object BuildGraphParser {

    private val LOG = Logger.getInstance(BuildGraphParser::class.java.name)

    fun parseOptions(doc: Document): HashSet<DataItem> {
        val options = hashSetOf<DataItem>()
        val nList = doc.getElementsByTagName("Option")
        for (i in 0 until nList.length) {

            // Name="CookNetworkDestination" Restrict="(\\.*)?" DefaultValue="" Description
            // element.getAttribute("Name")
            // element.getAttribute("Description")
            // element.getAttribute("DefaultValue")
            // element.getAttribute("Restrict")
            val element = nList.item(i) as Element
            options += DataItem(element.getAttribute("Name"), element.getAttribute("Description"))
        }
        return options
    }

    fun parseNodes(doc: Document): HashSet<DataItem> {
        val nodes = hashSetOf<DataItem>()
        val nList = doc.getElementsByTagName("Node")
        for (i in 0 until nList.length) {
            val element = nList.item(i) as Element
            nodes += DataItem(element.getAttribute("Name"), null)
        }
        return nodes
    }

    fun parseIncludes(doc: Document): HashSet<String> {
        val includes = hashSetOf<String>()
        val nList = doc.getElementsByTagName("Include")
        for (i in 0 until nList.length) {
            val element = nList.item(i) as Element
            includes += element.getAttribute("Script")
        }
        return includes
    }

    fun getDocument(inputStream: InputStream): Document? {
        return try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val xmlInput = InputSource(
                    StringReader(
                            inputStream.readBytes().toString(Charsets.UTF_8)
                    )
            )
            dBuilder.parse(xmlInput)
        } catch (e: Exception) {
            LOG.infoAndDebugDetails("Failed to read BuildGraph file", e)
            null
        }
    }
}