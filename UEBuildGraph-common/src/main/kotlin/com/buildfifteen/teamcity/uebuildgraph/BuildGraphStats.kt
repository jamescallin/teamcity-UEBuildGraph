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

typealias BuildGraphStatCollection = MutableMap<String, String>

class BuildGraphSequencedStatEntry(val time: Long, val value: String )
typealias BuildGraphSequencedStat = MutableList<BuildGraphSequencedStatEntry>
typealias BuildGraphSequencedStats = MutableMap<String, BuildGraphSequencedStat>

data class BuildGraphHierarchicalStat(val name: String, val value: String, val count: Int, val level: Int, val id: Int, val parentId: Int);
typealias BuildGraphHierarchicalSet = MutableList<BuildGraphHierarchicalStat>
typealias BuildGraphHierarchicalStats = MutableMap<String, BuildGraphHierarchicalSet>

