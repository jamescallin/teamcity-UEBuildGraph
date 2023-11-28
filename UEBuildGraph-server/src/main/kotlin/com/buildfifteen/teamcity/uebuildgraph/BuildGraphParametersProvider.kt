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

class BuildGraphParametersProvider {
    val keyUGSProject: String
        get() = BuildGraphConstants.KEY_UGS_PROJECT
    val keyUGSEnable: String
        get() = BuildGraphConstants.KEY_UGS_ENABLE
    val keyUGSBadge: String
        get() = BuildGraphConstants.KEY_UGS_BADGE
    val keyUGSNoStart: String
        get() = BuildGraphConstants.KEY_UGS_NOSTART
    val keyUGSChange: String
        get() = BuildGraphConstants.KEY_UGS_CHANGE

    val keyUATPath: String
        get() = BuildGraphConstants.KEY_UAT_PATH
    val keyScriptName: String
        get() = BuildGraphConstants.KEY_SCRIPT_NAME
    val keyNodeName: String
        get() = BuildGraphConstants.KEY_NODE_NAME
    val keyUseP4: String
        get() = BuildGraphConstants.KEY_USE_P4
    val keyGraphOptions: String
        get() = BuildGraphConstants.KEY_GRAPH_OPTIONS
    val keyReformatLog: String
        get() = BuildGraphConstants.KEY_REFORMAT_LOG
    val keyAddBuildMachine: String
        get() = BuildGraphConstants.KEY_BUILD_MACHINE
    val keyAdditionalParameters: String
        get() = BuildGraphConstants.KEY_ADDITIONAL_PARAMETERS
}