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

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class BuildGraphPropertiesProcessor : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        val uatPath = properties[BuildGraphConstants.KEY_UAT_PATH]
        if (PropertiesUtil.isEmptyOrNull(uatPath)) {
            result.add(InvalidProperty(BuildGraphConstants.KEY_UAT_PATH, "Should not be empty"))
        }
        val scriptName = properties[BuildGraphConstants.KEY_SCRIPT_NAME]
        if (PropertiesUtil.isEmptyOrNull(scriptName)) {
            result.add(InvalidProperty(BuildGraphConstants.KEY_SCRIPT_NAME, "Should not be empty"))
        }
        val nodeName = properties[BuildGraphConstants.KEY_NODE_NAME]
        if (PropertiesUtil.isEmptyOrNull(nodeName)) {
            result.add(InvalidProperty(BuildGraphConstants.KEY_NODE_NAME, "Should not be empty"))
        }

        val options = properties[BuildGraphConstants.KEY_GRAPH_OPTIONS]
        if (options != null && options.isNotBlank()) {
            val optionElements = options.split("§").toTypedArray();
            if (optionElements.size % 2 != 0) {
                result.add(InvalidProperty(BuildGraphConstants.KEY_GRAPH_OPTIONS, "Parsed an odd number of elements (expected key-value pairs)"))
            }

            var keys = optionElements.mapIndexedNotNull { index, el -> if ( index % 2 == 0) el else null }
            if (keys.size != keys.distinct().size ) {
                result.add(InvalidProperty(BuildGraphConstants.KEY_GRAPH_OPTIONS, "Keys should be unique!"))
            }
        }
        val ugsEnable = properties[BuildGraphConstants.KEY_UGS_ENABLE].toBoolean()
        if( ugsEnable ) {
            val ugsBadgeName = properties[BuildGraphConstants.KEY_UGS_BADGE]
            if (ugsBadgeName.isNullOrBlank()) {
                result.add(InvalidProperty(BuildGraphConstants.KEY_UGS_BADGE, "Should not be empty if UGS notifications are enabled"))
            }
            val ugsProjectPath = properties[BuildGraphConstants.KEY_UGS_PROJECT]
            if (ugsProjectPath.isNullOrBlank()) {
                result.add(InvalidProperty(BuildGraphConstants.KEY_UGS_PROJECT, "Should not be empty if UGS notifications are enabled"))
            }
        }

        return result
    }
}