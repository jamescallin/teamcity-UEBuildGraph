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

import jetbrains.buildServer.notification.ServiceMessageNotifier
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.SRunningBuild
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import java.lang.StringBuilder

class UGSServiceMessage(
    private val myUGSProjectFeatureHelper: UGSProjectFeatureHelper,
    private val mySBuildServer: SBuildServer
) : ServiceMessageNotifier {

    override  fun getServiceMessageNotifierType(): String {
        return BuildGraphConstants.SERVICE_MESSAGE_NAME
    }

    override fun sendBuildRelatedNotification(message: String, runningBuild: SRunningBuild, parameters: Map<String, String>) {
        // message can be ignored (left blank by the plugin
        // params:
        //   change - changelist
        //   status - Start|Success|Failure
        //   project - project path
        //   badge - name of the badge

        data class MsgPayload(
            val changeNumber: String,
            val buildType: String,
            val result: BuildDataResult,
            val url: String,
            val project: String,
            val archivePath: String = "",
        ) {
            override fun toString(): String {
                val output = StringBuilder("{")
                output.append(String.format("\"ChangeNumber\":\"%s\",",changeNumber))
                output.append(String.format("\"BuildType\":\"%s\",",buildType))
                output.append(String.format("\"Result\":\"%s\",",result.toString()))
                output.append(String.format("\"Url\":\"%s\",",url))
                output.append(String.format("\"Project\":\"%s\",",project.replace('\\', '/' )))
                output.append(String.format("\"ArchivePath\":\"%s\"",archivePath))
                output.append("}")
                return output.toString()            }
        }

        val change = parameters["change"] ?: ""
        val statusStr = parameters["status"] ?: ""
        val status = enumValueOf<BuildDataResult>(statusStr)
        val project = parameters["project"] ?: ""
        val badge = parameters["badge"] ?: ""

        val projectId = runningBuild.projectId ?: ""
        if(projectId.isNotEmpty()) {
            val uri = myUGSProjectFeatureHelper.getUgsServerUrl(projectId)
            if(uri.isNotEmpty()) {
                val url = String.format("%s/viewLog.html?buildId=%d", mySBuildServer.rootUrl, runningBuild.buildId)
                val msgPayload = MsgPayload( change, badge, status, url, project )
                postRequest(uri, msgPayload.toString() )
            }
        }
    }

    private fun postRequest(uri: String, requestBody: String): Int {
        val targetUri = String.format("%s/api/Build", uri )
        val httpPost = HttpPost( targetUri )
        val entity = StringEntity(requestBody, "UTF8")
        entity.setContentType("application/json")
        httpPost.entity = entity
        httpPost.setHeader("Content-type", "application/json")

        val timeout = 5
        val config = RequestConfig.custom()
            .setConnectTimeout(timeout * 1000)
            .setConnectionRequestTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000).build()

        HttpClientBuilder
            .create()
            .setDefaultRequestConfig(config)
            .build()
            .use { client ->
                client
                    .execute(
                        httpPost
                    ) .use { response ->
                        client.close()
                        return response.statusLine.statusCode
                    }
                }
    }
}