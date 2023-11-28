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

import com.buildfifteen.teamcity.uebuildgraph.BuildGraphConstants
import com.buildfifteen.teamcity.uebuildgraph.BuildGraphStatStore

class UELogParserCookStats(
    mWriter: UELogWriter,
    private val mBuildGraphStatsStore: BuildGraphStatStore? = null,) : UELogParserDefault(mWriter) {

    enum class State {
        DEFAULT,
        HIERARCHICAL_TIMER,
        MISC,
        COOK_PROFILE,
        DDC_SUMMARY,
        DDC_RESOURCES,
    }

    data class HierarchyTempData(val key: String, val value: String, val indent: Int, val count: Int)

    private var mCurrentState = State.DEFAULT
    private var mCurrentStatGroup: String? = null
    private var mHierarchicalStatList = mutableListOf<HierarchyTempData>()

    override fun onEnter(text: String, description: String) {
        mWriter.openBlock(BuildGraphConstants.BLOCK_COOKSTATS, description)
        super.onEnter(text, description)
    }

    override fun onLeave(text: String) {
        super.onLeave(text)
        mWriter.closeBlock(BuildGraphConstants.BLOCK_COOKSTATS)
    }

    override fun processLine(text: String) {
        when(mCurrentState) {
            State.DEFAULT            -> runStateDefault(text)
            State.MISC               -> runStateMiscStats(text)
            State.DDC_SUMMARY        -> runStateMiscStats(text)     // NB, intentionally this - group set manually in runStateDefault
            State.HIERARCHICAL_TIMER -> runStateHierarchicalTimer(text)
            State.COOK_PROFILE       -> runStateCookProfile(text)
//            State.DDC_RESOURCES      -> runStateDDCResources(text)
            else -> runStateDefault(text)
        }
    }

    private fun tryAddSimpleStat(text: String) : Boolean {
        lp_simpleStat.find(text)?.let {
            val (key, value) = it.destructured
            mBuildGraphStatsStore?.addStat(
                mCurrentStatGroup ?: "Unknown",
                key,
                value
            )
            mWriter.message(text)
            return true
        }
        return false
    }

    private fun trySetGroupName(text: String) : Boolean {
        lp_text.find(text)?.let {
            val (line) = it.destructured
            if(!line.startsWith("==")) {
                mCurrentStatGroup = line
            }
            mWriter.message(text)
            return true
        }
        return false
    }

    private fun tryAddProfileStat(text: String) : Boolean {
        lp_profileStat.find(text)?.let {
            val (indent, stat, value) = it.destructured
            mHierarchicalStatList.add(HierarchyTempData(stat, value, (indent.length / 3)-1, 0))
            mWriter.message(text)
            return true
        }
        return false
    }

    private fun tryAddTimerStat(text: String) : Boolean {
        lp_timerStat.find(text)?.let {
            val (indent, stat, value, count) = it.destructured
            mHierarchicalStatList.add(HierarchyTempData(stat, value, (indent.length - 2) shr 1, count.toInt()))
            mWriter.message(text)
            return true
        }
        return false
    }

    private fun recurseHierarchy(hierarchyName: String, currentIndent: Int, index: Int, parentIndex: Int): Int {
        var previousID = index
        var idx = index

        while(idx < mHierarchicalStatList.size) {
            if(mHierarchicalStatList[idx].indent < currentIndent )
                return idx
            else if(mHierarchicalStatList[idx].indent == currentIndent ) {
                mBuildGraphStatsStore?.addHierarchicalStat(
                    hierarchyName,
                    mHierarchicalStatList[idx].key,
                    mHierarchicalStatList[idx].value,
                    mHierarchicalStatList[idx].count,
                    mHierarchicalStatList[idx].indent,
                    idx,
                    parentIndex )
                previousID = idx
                idx++
            }
            else if(mHierarchicalStatList[idx].indent > currentIndent )
                idx = recurseHierarchy(hierarchyName, mHierarchicalStatList[idx].indent, idx, previousID)
        }
        return idx
    }

    private fun runStateDefault(text: String) {
        mWriter.message(text)
        mCurrentState = when {
            lp_cookProfileStart.containsMatchIn(text) -> State.COOK_PROFILE
            lp_miscStatsStart.containsMatchIn(text) -> State.MISC
            lp_ddcSummaryStart.containsMatchIn(text) -> State.DDC_SUMMARY
            lp_ddcResourcesStart.containsMatchIn(text) -> State.DDC_RESOURCES
            lp_hierarchyTimerStart.containsMatchIn(text) -> State.HIERARCHICAL_TIMER
            else -> State.DEFAULT
        }
        // new state setup:
        when(mCurrentState) {
            State.DDC_SUMMARY        -> { mCurrentStatGroup = "DDC Summary Stats" }
            State.COOK_PROFILE       -> { mHierarchicalStatList.clear() }
            State.HIERARCHICAL_TIMER -> { mHierarchicalStatList.clear() }
            else -> {}
        }
    }

    // runStateMiscStats also runs for the DDC Summary (with the group preset)
    private fun runStateMiscStats(text: String) {
        if( tryAddSimpleStat(text) ) return
        if( trySetGroupName(text) ) return

        // otherwise (it was a blank line and so) return to default state:
        mCurrentStatGroup = null
        mWriter.message(text)
        mCurrentState = State.DEFAULT
    }

    private fun runStateCookProfile(text: String) {
        if(tryAddProfileStat(text)) return
        if(text.endsWith("==")) {
            mWriter.message(text)
            return
        }

        // otherwise it was not a match, so write the stats to the store:
        recurseHierarchy("CookProfile", 0, 0, 0)
        mWriter.message(text)
        mCurrentState = State.DEFAULT
    }

    private fun runStateHierarchicalTimer(text: String) {
        if(tryAddTimerStat(text)) return

        recurseHierarchy("HierarchyTimer", 0, 0, 0)
        mWriter.message(text)
        mCurrentState = State.DEFAULT
    }

    companion object {
        private val lp_simpleStat = Regex("^.*:\\s+([^\\s=]+)\\s*=\\s*([^\\s=]+)")
        private val lp_profileStat = Regex("^[^:]*: [^:]*: ([\\d\\. ]+)([^=]+)=([\\d\\.]+)")
        private val lp_timerStat = Regex("^[^:]*: [^:]*: ([\\s]+)([^:]+): ([\\d\\.]+).*\\((\\d+)\\)")

        private val lp_text = Regex("^[^:]*: [^:]*:\\s+(.+)")

        private val lp_miscStatsStart = Regex("Display: Misc Cook Stats")
        private val lp_cookProfileStart = Regex("Display: Cook Profile")
        private val lp_ddcResourcesStart = Regex("DDC Resource Stats")
        private val lp_ddcSummaryStart = Regex("DDC Summary Stats")
        private val lp_hierarchyTimerStart = Regex("Hierarchy Timer Information")

    }

}