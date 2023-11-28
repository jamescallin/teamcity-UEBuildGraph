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

package com.buildfifteen.teamcity.uebuildgraph.logging

import com.buildfifteen.teamcity.uebuildgraph.BuildGraphMessage
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphMessageStore
import jetbrains.buildServer.agent.BuildProgressLogger

class UELogWriterStoreTeamCity(
    store: BuildGraphMessageStore,
    private val mLogger: BuildProgressLogger,
    private val mSummarisePostProcess: Boolean = true
) : UELogWriterStore(store) {

    private var mAssetValidationCount: Int = 0
    private var mAssetValidationFails: Int = 0

    override fun validateAsset(message: String) {
        mAssetValidationCount++
    }

    override fun validateFail(message: BuildGraphMessage) {
        mAssetValidationFails++
        super.validateFail(message)
    }

    override fun processFinished(exitCode: Int) {
        if(!mSummarisePostProcess)
            return
        val status : String = if((exitCode == 0) && (mAssetValidationFails==0)) "SUCCESS" else "FAILURE"
        val reportSummary = mStore.getSummary("")

        val justSuccess: Boolean = (exitCode == 0) && (mAssetValidationFails==0) && (reportSummary.numWarnings) == 0 && (reportSummary.numErrors == 0)
        var assetComma = ""

        if(justSuccess) {
            mLogger.message(TCLogMessage.status(status, ""))
        }
        else {
            var statusString = StringBuilder("(")
            if(mAssetValidationCount > 0) {
                if(mAssetValidationFails == 0)
                    statusString.append(String.format("%d tests passed",mAssetValidationCount))
                else
                    statusString.append(String.format("%d of %d tests failed",mAssetValidationFails, mAssetValidationCount))
                assetComma = ", "
            }
            if((reportSummary.numErrors - mAssetValidationFails) == 0 && reportSummary.numWarnings == 0) {
                statusString.append(")")
            }
            else if((reportSummary.numErrors - mAssetValidationFails) == 0) {
                statusString.append(String.format("%s%d warnings)", assetComma, reportSummary.numWarnings))
            }
            else if(reportSummary.numWarnings == 0) {
                statusString.append(String.format("%s%d errors)", assetComma, reportSummary.numErrors - mAssetValidationFails))
            }
            else {
                statusString.append(String.format("%s%d errors, %d warnings)", assetComma, (reportSummary.numErrors - mAssetValidationFails), reportSummary.numWarnings))
            }
            mLogger.message(TCLogMessage.status(status, statusString.toString()))
        }
    }
}
