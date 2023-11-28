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
import {PluginContext} from "@jetbrains/teamcity-api";
import {ContentPanel} from "@jetbrains/teamcity-api/components";
import LoaderInline from "@jetbrains/ring-ui/components/loader-inline/loader-inline";
import Link from '@jetbrains/ring-ui/components/link/link';
import List from "@jetbrains/ring-ui/components/list/list";

import styles from './BuildSummary.css'

const EmptyArray: any[] = [];
Object.freeze(EmptyArray);

interface BuildSummary {
    numErrors: number,
    numErrorsUnique: number,
    numWarnings: number,
    numWarningsUnique: number,
    name: string,
}

function BuildSummary({location}: PluginContext): React.ReactNode {
    const {buildId} = location;
    const [isLoading, setIsLoading] = React.useState(true);
    const [buildSummary, setBuildSummary] = React.useState<Array<BuildSummary>>(EmptyArray);
    const [buildSummaryTableData, setBuildSummaryTableData] = React.useState<Array<any>>(EmptyArray);
    const [xlsxFilePathBuild, setXlsxFilePathBuild] = React.useState("");
    const [xlsxFilePathAsset, setXlsxFilePathAsset] = React.useState("");

    React.useEffect( () => {
        const requestSummary = async () => {
            try {
                const result: Array<BuildSummary> = await utils.requestJSON(`app/rest/builds/id:${buildId}/artifacts/content/uebuildgraph/build-summary.json`);
                setBuildSummary(result);
                setIsLoading(false);
            }
            catch {
                setIsLoading(false);
            }
        }
        const requestXLSXInfo = async () => {
            try {
                const result: Array<BuildSummary> = await utils.requestJSON(`app/rest/builds/id:${buildId}/artifacts/children/uebuildgraph`);
                const xlsxFileAssets = result['file'].find((file) => file.name.startsWith("assets") && file.name.endsWith("xlsx") );
                if(xlsxFileAssets) {
                    setXlsxFilePathAsset(xlsxFileAssets.content.href)
                }
                const xlsxFileBuild = result['file'].find((file) => file.name.startsWith("build") && file.name.endsWith("xlsx") );
                if(xlsxFileBuild) {
                    setXlsxFilePathBuild(xlsxFileBuild.content.href);
                }
            }
            catch {}
        }
        setIsLoading(true);
        requestSummary();
        requestXLSXInfo();
    }, [buildId] )

    React.useEffect( () => {
        const tableData = (buildSummary.map( phase => { return [
            {
                rgItemType: List.ListProps.Type.TITLE,
                label: phase.name,
            },
            {
                label: (<div className={styles.uebgSummary}><span className={styles.name}><strong>Errors</strong></span><span className={styles.count}>{phase.numErrors}</span><span className={styles.unique}>({phase.numErrorsUnique})</span></div>),
                rgItemType: List.ListProps.Type.ITEM,
                description: 'Unique errors in brackets',
            },
            {
                label: (<div className={styles.uebgSummary}><span className={styles.name}><strong>Warnings</strong></span><span className={styles.count}>{phase.numWarnings}</span><span className={styles.unique}>({phase.numWarningsUnique})</span></div>),
                rgItemType: List.ListProps.Type.ITEM,
                description: 'Unique warnings in brackets',
            },
        ]
        }) );
        setBuildSummaryTableData(tableData.flatten())
    }, [buildSummary]);

    if( isLoading )
        return <LoaderInline />;

    if(buildSummaryTableData && buildSummaryTableData.length > 0)
        return (
            <ContentPanel
                className={styles.contentPanel}
                panelType={"expandable"}
                expandedByDefault={true}
                heading={"UE Build Graph Overview"}
                content={(
                    <div>
                        <List
                            data={buildSummaryTableData}
                            compact={true}
                        />
                        {xlsxFilePathBuild && <Link href={xlsxFilePathBuild}>Download all Build warnings and errors as XLSX file</Link>}
                        {xlsxFilePathAsset && <Link href={xlsxFilePathAsset}>Download all Validation warnings and errors as XLSX file</Link>}
                    </div>
                )}
            />);

    return null;    // no data == no display
}

export default BuildSummary
