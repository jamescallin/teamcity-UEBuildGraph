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

import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor

class UGSProjectFeatureHelper(
    private val myProjectManager: ProjectManager,
) {

    fun getOwnUgsServerUrl(project: SProject) : String {
        val feature = project.getOwnFeaturesOfType(UGS_FEATURE_NAME).firstOrNull() ?: return ""
        return feature.parameters[UGS_SERVER_URL] ?: ""
    }

    fun getUgsServerUrl(project: SProject) : String {
        val feature = project.getAvailableFeaturesOfType(UGS_FEATURE_NAME).firstOrNull() ?: return ""
        return feature.parameters[UGS_SERVER_URL] ?: ""
    }

    fun getUgsServerUrl(projectId: String) : String {
        val project: SProject = myProjectManager.findProjectById(projectId) ?: return ""
        val feature = project.getAvailableFeaturesOfType(UGS_FEATURE_NAME).firstOrNull() ?: return ""
        return feature.parameters[UGS_SERVER_URL] ?: ""
    }

    @Throws(Exception::class)
    fun getUgsFeature(projectId: String) : SProjectFeatureDescriptor? {
        val project: SProject = myProjectManager.findProjectById(projectId) ?: throw Throwable("UGSProjectFeatureHelper::getUgsFeature - Project not found")
        return project.getOwnFeaturesOfType(UGS_FEATURE_NAME).firstOrNull()
    }

    @Throws(Exception::class)
    fun getOrCreateUgsFeature(projectId: String) : SProjectFeatureDescriptor {
        val project: SProject = myProjectManager.findProjectById(projectId) ?: throw Throwable("UGSProjectFeatureHelper::getUgsFeature - Project not found")
        return getUgsFeature(projectId) ?: project.addFeature(UGS_FEATURE_NAME, emptyMap()).also { project.persist() }
    }

    @Throws(Exception::class)
    fun updateUgsFeature(projectId: String, feature: SProjectFeatureDescriptor, serverUrl: String) {
        val project: SProject = myProjectManager.findProjectById(projectId) ?: throw Throwable("UGSProjectFeatureHelper::getUgsFeature - Project not found")
        val newParams = feature.parameters.toMutableMap()
        newParams[UGS_SERVER_URL] = serverUrl
        project.updateFeature(feature.id, feature.type, newParams)
        project.persist()
    }

    @Throws(Exception::class)
    fun updateUgsFeature(project: SProject, serverUrl: String) {
        val feature = project.getOwnFeaturesOfType(UGS_FEATURE_NAME).firstOrNull() ?: project.addFeature(UGS_FEATURE_NAME, emptyMap())
        val newParams = feature.parameters.toMutableMap()
        newParams[UGS_SERVER_URL] = serverUrl
        project.updateFeature(feature.id, feature.type, newParams)
        project.persist()
    }

    companion object {
        const val UGS_FEATURE_NAME = "ugs_project_features"
        const val UGS_SERVER_URL = "ugs_server_url"
    }

}