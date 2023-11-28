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

import com.buildfifteen.teamcity.uebuildgraph.logging.*
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.ProcessListener
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.TCStreamUtil
import java.io.File
import java.io.IOException


class BuildGraphService(private val mArtifactsWatcher: ArtifactsWatcher) : BuildServiceAdapter() {

    private lateinit var mLogListener: UELogListener
    private lateinit var mPathStripper: FilePathStripper
    private val mBuildGraphMessageStore = BuildGraphMessageStore()
    private val mBuildGraphMessageStoreValidateAsset = BuildGraphMessageStoreValidateAsset()
    private val mBuildGraphStatStore = BuildGraphStatStore()
    private val mLogWriters = UELogWriters()
    private val mFilesToDelete = mutableSetOf<File>()

    override fun afterInitialized() {
        super.afterInitialized()
        if(runnerParameters[BuildGraphConstants.KEY_REFORMAT_LOG] != null) {

            var ugsInfo : UGSInfo? = null;
            var ugsSuppressStartNotifications = false
            val ugsNotificationsEnabled = runnerParameters[BuildGraphConstants.KEY_UGS_ENABLE]?.isNotEmpty() == true
            if(ugsNotificationsEnabled) {
                val ugsChange : String = runnerParameters[BuildGraphConstants.KEY_UGS_CHANGE] ?: run {
                    val vcsRoot = build.vcsRootEntries.firstOrNull()
                    if(vcsRoot != null ) {
                        build.getBuildCurrentVersion(vcsRoot.vcsRoot)
                    }
                    else {
                        ""
                    }
                }
                val ugsProject = runnerParameters[BuildGraphConstants.KEY_UGS_PROJECT] ?: ""
                val ugsBadge = runnerParameters[BuildGraphConstants.KEY_UGS_BADGE] ?: ""
                ugsSuppressStartNotifications = runnerParameters[BuildGraphConstants.KEY_UGS_NOSTART]?.isNotEmpty() ?: false
                if(ugsChange.isNotBlank() && ugsProject.isNotBlank() && ugsBadge.isNotBlank())
                    ugsInfo = UGSInfo(ugsProject, ugsChange, ugsBadge )
            }
            mLogWriters.addWriter(UELogWriterTeamCity(this.logger, ugsInfo, ugsNotificationsEnabled, ugsSuppressStartNotifications))
            mLogWriters.addWriter(UELogWriterStoreTeamCity(mBuildGraphMessageStore, this.logger, true))
            mLogWriters.addWriter(UELogWriterStoreTeamCity(mBuildGraphMessageStoreValidateAsset, this.logger, false))
        }
        else  {
            mLogWriters.addWriter(UELogWriterDefault(this.logger))
            mLogWriters.addWriter(UELogWriterStore(mBuildGraphMessageStore))
            mLogWriters.addWriter(UELogWriterStore(mBuildGraphMessageStoreValidateAsset))
        }
        //todo: tidy this up and make it conditional
        mPathStripper = FilePathStripper(runnerContext.workingDirectory.absolutePath)
        mLogListener = UELogListener(mLogWriters, mPathStripper, mBuildGraphStatStore)
    }

    @Throws(RunBuildException::class)
    override fun makeProgramCommandLine(): ProgramCommandLine {
        val commandFileName = writeCommandFile()
        setExecutableAttribute(commandFileName)

        return SimpleProgramCommandLine(
                runnerContext,
                commandFileName,
                emptyList()
        )
    }

    override fun getListeners(): List<ProcessListener> {
        return listOf(
                mLogListener,
        )
    }

    override fun afterProcessFinished() {
        super.afterProcessFinished()
        var didExportMessages = false

        // export the errors and warnings:
        if(mBuildGraphMessageStore.hasMessages()) {
            mBuildGraphMessageStore.exportJSON(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_REPORT_JSON)

            val reportFileNameExcel = String.format(BuildGraphConstants.ARTIFACTS_REPORT_XLSX_FMT, this.build.buildNumber)
            mBuildGraphMessageStore.exportXLSX(buildTempDirectory.path + File.separator + reportFileNameExcel)
            didExportMessages = true
        }

        if(mBuildGraphMessageStoreValidateAsset.hasMessages()) {
            mBuildGraphMessageStoreValidateAsset.exportJSON(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_ASSETS_JSON)

            val assetsFileNameExcel = String.format(BuildGraphConstants.ARTIFACTS_ASSETS_XLSX_FMT, this.build.buildNumber)
            mBuildGraphMessageStoreValidateAsset.exportXLSX(buildTempDirectory.path + File.separator + assetsFileNameExcel)
            didExportMessages = true
        }

        // export any stats:
        if(mBuildGraphStatStore.hasStats()) {
            mBuildGraphStatStore.exportStatsJSON(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_STATS_JSON)
            mBuildGraphStatStore.exportHierarchyStatsJSON(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_HIERARCHICAL_STATS_JSON)
            mBuildGraphStatStore.exportSequencedStatsJSON(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_SEQUENCE_STATS_JSON)

            if(!didExportMessages) {
                mArtifactsWatcher.addNewArtifactsPath(
                    buildTempDirectory.path + File.separator + "*.json" + "=>" + BuildGraphConstants.ARTIFACTS_BASE_DIR
                )
            }
        }

        if( didExportMessages ) {
            // create the summary JSON.  Use the main Build Graph Message Store by default.  If that's empty check that the Validate Asset one (though this is likely empty too)
            if(mBuildGraphMessageStore.hasMessages())
                BuildGraphMessageStore.exportCombinedSummary(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_SUMMARY_FILE, arrayOf(mBuildGraphMessageStore))
            else if(mBuildGraphMessageStoreValidateAsset.hasMessages())
                BuildGraphMessageStore.exportCombinedSummary(buildTempDirectory.path + File.separator + BuildGraphConstants.ARTIFACTS_SUMMARY_FILE, arrayOf(mBuildGraphMessageStoreValidateAsset))
            mArtifactsWatcher.addNewArtifactsPath(
                buildTempDirectory.path + File.separator + "*.json" + "=>" + BuildGraphConstants.ARTIFACTS_BASE_DIR
            )
            mArtifactsWatcher.addNewArtifactsPath(
                buildTempDirectory.path + File.separator + "*.xlsx" + "=>" + BuildGraphConstants.ARTIFACTS_BASE_DIR
            )
        }

        // now clear out the temp files:
        for (file in mFilesToDelete) {
            FileUtil.delete(file)
        }
        mFilesToDelete.clear()
    }

    @Throws(RunBuildException::class)
    private fun writeCommandFile(): String {
        return try {
            val systemInfo = this.agentConfiguration.systemInfo
            val commandFile = if (systemInfo.isWindows)
                                    File.createTempFile("custom_script", ".cmd", agentTempDirectory)
                                else
                                    File.createTempFile("custom_script", ".sh", agentTempDirectory)

            // Create the Script Content, starting with the UAT command, or teh default if it's blank:
            val scriptContent = StringBuilder("${runnerParameters[BuildGraphConstants.KEY_UAT_PATH] ?: BuildGraphConstants.DEFAULT_UAT_PATH}")
            // the actual command:
            scriptContent.append(" BuildGraph ")
            // now the -Script and -Target params:
            scriptContent.append(String.format("-Script=\"%s\" ", runnerParameters[BuildGraphConstants.KEY_SCRIPT_NAME]))
            scriptContent.append(String.format("-Target=\"%s\" ", runnerParameters[BuildGraphConstants.KEY_NODE_NAME]))
            // optionally add the -noP4 param:
            if( runnerParameters[BuildGraphConstants.KEY_USE_P4] != null ) {
                scriptContent.append("-noP4 ")
            }
            if( runnerParameters[BuildGraphConstants.KEY_BUILD_MACHINE] != null ) {
                scriptContent.append("-buildmachine ")
            }
            // add any additional parameters:
            if( runnerParameters[BuildGraphConstants.KEY_ADDITIONAL_PARAMETERS] != null ) {
                scriptContent.append("${runnerParameters[BuildGraphConstants.KEY_ADDITIONAL_PARAMETERS]} ")
            }
            // now add the Options:
            val options = runnerParameters[BuildGraphConstants.KEY_GRAPH_OPTIONS]
            if (!options.isNullOrBlank()) {
                optionsRegEx.findAll(options).forEach {
                    val quotes = if(it.groupValues[2].contains(" ")) "\"" else ""
                    scriptContent.append("-set:${it.groupValues[1]}=${quotes}${it.groupValues[2]}${quotes} ")
                }
            }

            logger.message(scriptContent.toString())

            FileUtil.writeFileAndReportErrors(commandFile, scriptContent.toString())
            mFilesToDelete.add(commandFile)
            commandFile.absolutePath
        } catch (e: IOException) {
            val exception = RunBuildException("Failed to create temporary custom script file in directory '$agentTempDirectory': " + e
                    .toString(), e)
            exception.isLogStacktrace = false
            throw exception
        }
    }

    @Throws(RunBuildException::class)
    private fun setExecutableAttribute(script: String) {
        try {
            TCStreamUtil.setFileMode(File(script), "a+x")
        } catch (t: Throwable) {
            throw RunBuildException("Failed to set executable attribute for custom script '$script'", t)
        }
    }

    companion object {
        private val optionsRegEx = Regex("([^§]*)§([^§]*)(?:§|\$)")
    }
}