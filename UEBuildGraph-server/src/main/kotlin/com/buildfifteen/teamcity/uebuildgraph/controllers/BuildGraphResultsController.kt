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

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.ContentSecurityPolicyConfig
import jetbrains.buildServer.web.openapi.*
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class BuildGraphResultsController(
    private val myPluginDescriptor: PluginDescriptor,
    places: PagePlaces,
    controllerManager: WebControllerManager,
    contentSecurityPolicyConfig: ContentSecurityPolicyConfig
) : BaseController() {
    init {
        val url = "/uebuildgraphresults.html"
        val pageExtension = SimplePageExtension(places)
        pageExtension.pluginName = PLUGIN_NAME
        pageExtension.placeId = PlaceId.ALL_PAGES_FOOTER_PLUGIN_CONTAINER
        pageExtension.includeUrl = url
        pageExtension.register()
        controllerManager.registerController(url, this)
        contentSecurityPolicyConfig.addDirectiveItems("script-src", BUNDLE_DEV_URL)
    }

    @Throws(Exception::class)
    override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
        val mv = ModelAndView(myPluginDescriptor.getPluginResourcesPath("results-spa.jsp"))
        mv.model["PLUGIN_NAME"] = PLUGIN_NAME
        return mv
    }

    companion object {
        private const val PLUGIN_NAME = "UEBuildGraph-UI"
        private const val BUNDLE_DEV_URL = "http://localhost:8080"
    }
}