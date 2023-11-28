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
package com.buildfifteen.teamcity.uebuildgraph.controllers

import com.buildfifteen.teamcity.uebuildgraph.UGSProjectFeatureHelper
import jetbrains.buildServer.controllers.ActionErrors
import jetbrains.buildServer.controllers.BaseFormXmlController
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.*
import org.jdom.Element
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UGSProjectController(
    private val myPluginDescriptor: PluginDescriptor,
    myControllerManager: WebControllerManager,
    private val myProjectManager: ProjectManager,
    private val myUGSProjectFeatureHelper: UGSProjectFeatureHelper,
) : BaseFormXmlController() {

    init {
        myControllerManager.registerController(SETTINGS_URL, this)
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) = null

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse, xmlResponse: Element) {
        val errors = ActionErrors()
        try {
            val projectId = request.getParameter("projectId") ?: throw Throwable("UGSProjectController:doPost() - projectId must be specified in request")
            val project: SProject = myProjectManager.findProjectById(projectId)
                ?: throw Throwable("Could not find project for projectId specified in request")

            val serverUrl = request.getParameter("serverUrl") ?: throw Throwable("UGSProjectController:doPost() - serverUrl must be specified in request")
            myUGSProjectFeatureHelper.updateUgsFeature(project, serverUrl)
        }
        catch (e: Throwable) {
            errors.addError("UGSSettings", e.message)
        }

        if (errors.hasErrors()) {
            errors.serialize(xmlResponse)
        }
        response.status = HttpServletResponse.SC_NO_CONTENT
    }

    companion object {
        const val SETTINGS_URL = "/ugssettings.html"
    }
}