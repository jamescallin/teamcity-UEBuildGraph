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

object BuildGraphConstants {
    const val RUNNER_TYPE = "UEBuildGraphRunner"
    const val SERVICE_MESSAGE_NAME = "UGSNotification"

    // Misc constants:
    const val ARTIFACTS_BASE_DIR = "uebuildgraph"
    const val ARTIFACTS_REPORT_JSON = "build-report.json"
    const val ARTIFACTS_REPORT_XLSX_FMT = "build-report-%s.xlsx"
    const val ARTIFACTS_ASSETS_JSON = "assets-report.json"
    const val ARTIFACTS_ASSETS_XLSX_FMT = "assets-report-%s.xlsx"
    const val ARTIFACTS_SUMMARY_FILE = "build-summary.json"
    const val ARTIFACTS_STATS_JSON = "stats.json"
    const val ARTIFACTS_SEQUENCE_STATS_JSON = "stats-sequence.json"
    const val ARTIFACTS_HIERARCHICAL_STATS_JSON = "stats-hierarchy.json"

    // Field names
    const val KEY_UAT_PATH = "key_UAT_PATH"
    const val KEY_SCRIPT_NAME = "key_SCRIPT_NAME"
    const val KEY_NODE_NAME = "key_NODE_NAME"
    const val KEY_USE_P4 = "key_USE_P4"
    const val KEY_BUILD_MACHINE = "key_BUILD_MACHINE"
    const val KEY_GRAPH_OPTIONS = "key_GRAPH_PARAMETERS"
    const val KEY_REFORMAT_LOG = "key_REFORMAT_LOG"
    const val KEY_ADDITIONAL_PARAMETERS = "key_ADDITIONAL_PARAMETERS"

    // UGS Field Names:
    const val KEY_UGS_ENABLE = "key_UGS_ENABLE"
    const val KEY_UGS_PROJECT = "key_UGS_PROJECT"
    const val KEY_UGS_BADGE = "key_UGS_BADGE_NAME"
    const val KEY_UGS_NOSTART = "key_UGS_NOSTART"
    const val KEY_UGS_CHANGE = "key_UGS_CHANGE"

    // Block Names
    const val BLOCK_DEFAULT = "Default"
    const val BLOCK_COMPILER = "Compiler"
    const val BLOCK_CONTENT = "Content"
    const val BLOCK_COOKSTATS = "Cook Stats"
    const val BLOCK_TOOL = "UBT"
    const val BLOCK_TASK = "Task"
    const val BLOCK_STAGE = "Stage"
    const val BLOCK_PACKAGE = "Package"
    const val BLOCK_GAUNTLET = "Gauntlet"
    const val BLOCK_RUNEDITOR = "Run Editor"
    const val BLOCK_VALIDATEASSET = "Asset Validation"

    // defaults
    const val DEFAULT_UAT_PATH = "Engine\\Build\\BatchFiles\\RunUAT.bat"
}