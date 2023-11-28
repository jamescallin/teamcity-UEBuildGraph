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

import com.buildfifteen.teamcity.uebuildgraph.UGSProjectFeatureHelper
import com.buildfifteen.teamcity.uebuildgraph.controllers.UGSProjectController
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.controllers.admin.projects.EditProjectTab
import jetbrains.buildServer.web.openapi.*
import javax.servlet.http.HttpServletRequest

class UGSProjectTab(
    pagePlaces: PagePlaces,
    myPluginDescriptor: PluginDescriptor,
    private val myUGSProjectFeatureHelper: UGSProjectFeatureHelper,
) : EditProjectTab(
    pagePlaces,
    "ugsProjectTab",
    myPluginDescriptor.getPluginResourcesPath("ugsProjectTab.jsp"),
    "UGS Metadata Server")
{
    private val myLogger = Logger.getInstance(UGSProjectTab::class.qualifiedName)

    init {
        setPosition(PositionConstraint.last())
        register()
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest) {
        super.fillModel(model, request)

        val currentProject = getProject(request) ?: return
        val serverUrl = myUGSProjectFeatureHelper.getOwnUgsServerUrl(currentProject)
        model["serverUrl"] = serverUrl
        if( serverUrl.isEmpty() )
            model["parentServerUrl"] = myUGSProjectFeatureHelper.getUgsServerUrl(currentProject)
        else
            model["parentServerUrl"] = "x"
        model["projectId"] = currentProject.projectId
        model["ugsSettingsEndpoint"] = UGSProjectController.SETTINGS_URL
    }
}
