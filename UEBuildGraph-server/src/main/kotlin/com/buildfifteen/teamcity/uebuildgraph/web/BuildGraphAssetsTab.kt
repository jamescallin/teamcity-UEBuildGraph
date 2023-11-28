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

package com.buildfifteen.teamcity.uebuildgraph.web

import com.buildfifteen.teamcity.uebuildgraph.BuildGraphConstants
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.ViewLogTab
import java.io.File
import javax.servlet.http.HttpServletRequest

class BuildGraphAssetsTab( pagePlaces: PagePlaces,
                         server: SBuildServer,
                         descriptor: PluginDescriptor ) : ViewLogTab(TAB_TITLE, TAB_CODE, pagePlaces, server)
{
    init {
        includeUrl = descriptor.getPluginResourcesPath("assets-report-tab.jsp")
        register()
    }

    override fun fillModel(model: MutableMap<String, Any?>,
                           request: HttpServletRequest,
                           build: SBuild ) {
        model["tc_buildid"] = build.buildId
    }

    override fun isAvailable(request: HttpServletRequest, build: SBuild): Boolean {
        val buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
        val reportArtifact = buildArtifacts.findArtifact(BuildGraphConstants.ARTIFACTS_BASE_DIR + File.separator + BuildGraphConstants.ARTIFACTS_ASSETS_JSON)
        return reportArtifact.isAvailable
    }

    companion object {
        private const val TAB_TITLE = "Asset Report"
        private const val TAB_CODE = "uebgAssetReportTab"
    }
}