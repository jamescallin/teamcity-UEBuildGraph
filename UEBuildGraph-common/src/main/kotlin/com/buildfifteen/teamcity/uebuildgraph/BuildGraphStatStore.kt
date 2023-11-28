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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class BuildGraphStatStore {

    private var mStatGroups = mutableMapOf<String, BuildGraphStatCollection>()
    private val mSequencedStats: BuildGraphSequencedStats = mutableMapOf()
    private var mHierarchyStats: BuildGraphHierarchicalStats = mutableMapOf()

    fun addStat(group: String, name: String, value: String) {
        if(!mStatGroups.containsKey(group)) {
            mStatGroups[group] = mutableMapOf()
        }
        mStatGroups[group]!![name] = value
    }

    fun hasStats() : Boolean {
        return mStatGroups.isNotEmpty() || mSequencedStats.isNotEmpty() || mHierarchyStats.isNotEmpty()
    }

    fun addSequencedStat(statName: String, time: Long, value: String) {
        if(!mSequencedStats.containsKey(statName)) {
            mSequencedStats[statName] = mutableListOf()
        }
        mSequencedStats[statName]?.add(BuildGraphSequencedStatEntry(time, value))
    }

    fun addHierarchicalStat(hierarchyName: String, name: String, value: String, count: Int, level: Int, id: Int, parentId: Int) {
        if(!mHierarchyStats.containsKey(hierarchyName)) {
            mHierarchyStats[hierarchyName] = mutableListOf()
        }
        mHierarchyStats[hierarchyName]?.add( BuildGraphHierarchicalStat( name, value, count, level, id, parentId))
    }

    fun exportStatsJSON(filename: String) {
        if(mStatGroups.isEmpty())
            return
        val outputFile = File(filename)
        val statsType: Type = object : TypeToken<Map<String?, BuildGraphStatCollection?>?>() {}.getType()
        outputFile.writeText( Gson().toJson(mStatGroups, statsType) )
    }

    fun exportHierarchyStatsJSON(filename: String) {
        if(mHierarchyStats.isEmpty())
            return
        val outputFile = File(filename)
        val statsType: Type = object : TypeToken<Map<String?, BuildGraphHierarchicalSet?>?>() {}.getType()
        outputFile.writeText( Gson().toJson(mHierarchyStats, statsType) )
    }

    fun exportSequencedStatsJSON(filename: String) {
        if(mSequencedStats.isEmpty())
            return
        val outputFile = File(filename)
        outputFile.writeText( Gson().toJson(mSequencedStats) )
    }
}