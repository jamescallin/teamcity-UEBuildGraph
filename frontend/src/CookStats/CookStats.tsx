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

import {React, utils} from "@jetbrains/teamcity-api"
import LoaderInline from "@jetbrains/ring-ui/components/loader-inline/loader-inline";
import {Tabs, Tab} from "@jetbrains/ring-ui/components/tabs/tabs";

import CookStatsBasic from "./CookStatsBasic";
import CookStatsHierarchical from "./CookStatsHierarchical";
import CookStatsSeq from "./CookStatsSeq";

const EmptyArray: any[] = [];
Object.freeze(EmptyArray);

type StatSetType = {
    [id: string]: string;
}

type StatList = {
    [id: string]: StatSetType;
}

function CookStats({buildid}) {
    const [isLoading, setIsLoading] = React.useState(true);
    const [statList, setStatList] = React.useState<Array<StatList>>(EmptyArray);
    const [statSequences, setStatSequences] = React.useState({progress: [], diagnostics: [], ofhMax: 0, vmMax: 0});
    const [statHierarchies, setStatHierarchies] = React.useState({})
    const [selectedTab, setSelectedTab] = React.useState('cook-stats');

    React.useEffect( () => {
        const requestStats = async () => {
            try {
                const resultList: Array<StatList> = await utils.requestJSON(`app/rest/builds/id:${buildid}/artifacts/content/uebuildgraph/stats.json`);
                setStatList(resultList);
                const resultSeq: Array<StatList> = await utils.requestJSON(`app/rest/builds/id:${buildid}/artifacts/content/uebuildgraph/stats-sequence.json`);
                const progress = resultSeq.Cooker_Cooked.map((e, i) => {
                    return {
                        time:e.time,
                        cooked: e.value,
                        remain: resultSeq.Cooker_Remain[i].value,
                    };
                });
                let ofhMax = resultSeq.OpenFileHandles[0].value;
                let vmMax = resultSeq.VirtualMemory[0].value;
                const diagnostics = resultSeq.OpenFileHandles.map((e,i) => {
                    ofhMax = Math.max(ofhMax, e.value);
                    const vmVal = resultSeq.VirtualMemory[i].value;
                    vmMax = Math.max(vmMax, vmVal);
                    return {
                        time: e.time,
                        OpenFileHandles: e.value,
                        VirtualMemory: vmVal,
                    }
                })
                setStatSequences({ progress, diagnostics, ofhMax, vmMax });
                const resultHier: Array<StatList> = await utils.requestJSON(`app/rest/builds/id:${buildid}/artifacts/content/uebuildgraph/stats-hierarchy.json`);
                setStatHierarchies(resultHier)

                setIsLoading(false);
            }
            catch {
                setIsLoading(false);
            }
        }
        setIsLoading(true);
        requestStats();
    }, [buildid] )

    if( isLoading )
        return <LoaderInline />;

    if(statList && statSequences.diagnostics.length > 0)
        return (
            <Tabs selected={selectedTab} onSelect={selected => setSelectedTab(selected)}>
                <Tab id="cook-stats" title="Cook Stats">
                    <CookStatsBasic statList={statList} />
                </Tab>
                <Tab id="hierarchical-stats" title="Hierarchical Stats">
                    <CookStatsHierarchical statHierarchy={statHierarchies} />
                </Tab>
                <Tab id="sequence-stats" title="Sequenced Stats">
                    <CookStatsSeq statSequences={statSequences} />
                </Tab>
            </Tabs>
        );

    return null;    // no data == no display
}

export default CookStats;
