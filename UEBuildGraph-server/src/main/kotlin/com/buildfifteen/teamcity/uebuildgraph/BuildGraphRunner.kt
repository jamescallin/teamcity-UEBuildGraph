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

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor

class BuildGraphRunner(private val _descriptor: PluginDescriptor,
                       registry: RunTypeRegistry) : RunType() {
    override fun getType(): String {
        return BuildGraphConstants.RUNNER_TYPE
    }

    override fun getDisplayName(): String {
        return "Unreal Engine BuildGraph Runner"
    }

    override fun getDescription(): String {
        return "Run UAT BuildGraph commands more cleanly."
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return BuildGraphPropertiesProcessor()
    }

    override fun getEditRunnerParamsJspFilePath(): String? {
        return _descriptor.getPluginResourcesPath("runner-edit.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String? {
        return _descriptor.getPluginResourcesPath("runner-view.jsp")
    }

    override fun getDefaultRunnerProperties(): Map<String, String>? {
        val defaults: HashMap<String, String> = HashMap<String, String>()
        defaults[BuildGraphConstants.KEY_UAT_PATH] = BuildGraphConstants.DEFAULT_UAT_PATH
        defaults[BuildGraphConstants.KEY_REFORMAT_LOG] =  true.toString()
        defaults[BuildGraphConstants.KEY_BUILD_MACHINE] =  true.toString()
        defaults[BuildGraphConstants.KEY_UGS_ENABLE] =  false.toString()
        return defaults
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        return "Node: " + parameters[BuildGraphConstants.KEY_NODE_NAME]
    }

    init {
        registry.registerRunType(this)
    }
}