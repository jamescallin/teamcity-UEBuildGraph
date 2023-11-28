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
import {SelectionItem} from "@jetbrains/ring-ui/components/table/selection";
import {H3} from '@jetbrains/ring-ui/components/heading/heading'

import styles from './AssetReport.css'
import Link from "@jetbrains/ring-ui/components/link/link";

const EmptyArray: any[] = [];
Object.freeze(EmptyArray);

interface BuildResults extends SelectionItem {
    occurrences: number,
    block: string,
    source: string,
    type: string,
    code: string,
    message: string,
    indent: number,
}

type AssetProblem = {
    [type: string]: string;
}

type AssetProblemList = {
    [name: string]: AssetProblem;
}

const Cell = ({children}) => (
    <div className={styles.col}>{children}</div>
);

function Assets({assets}) {
    return (
        <div className={styles.assetReportTable}>
            {   assets.map(({name, problems}) => (
                <React.Fragment>
                    <header><Cell><H3>{name}</H3></Cell><Cell /><Cell /></header>
                    {problems.map( ({type, message, isError}) => (
                        <div className={isError?styles.errorRow:styles.warningRow}>
                            <Cell />
                            <Cell>{type}</Cell>
                            <Cell>{message}</Cell>
                        </div>
                    ) )}
                </React.Fragment>
            ))
            }
        </div>
    );
}

function AssetReport({buildid}) {
    const [isLoading, setIsLoading] = React.useState(true);
    const [buildResults, setBuildResults] = React.useState<Array<BuildResults>>(EmptyArray);
    const [assetProblemList, setAssetProblemList] = React.useState<Array<AssetProblemList>>(EmptyArray);
    const [xlsxFilePath, setXlsxFilePath] = React.useState("");

    React.useEffect( () => {
        const requestResults = async () => {
            try {
                const result: Array<BuildResults> = await utils.requestJSON(`app/rest/builds/id:${buildid}/artifacts/content/uebuildgraph/assets-report.json`);
                setBuildResults(result);
                setIsLoading(false);
            }
            catch {
                setIsLoading(false);
            }
        }
        const requestXLSXInfo = async () => {
            try {
                const result = await utils.requestJSON(`app/rest/builds/id:${buildid}/artifacts/children/uebuildgraph`);
                const xlsxFile = result['file'].find((file) => file.name.startsWith("assets") && file.name.endsWith("xlsx") );
                if(xlsxFile) {
                    setXlsxFilePath(xlsxFile.content.href);
                }
            }
            catch {}
        }
        setIsLoading(true);
        requestResults();
        requestXLSXInfo();
    }, [buildid] )

    React.useEffect( () => {
        if( buildResults.length > 0 ) {
            var problemsObj = buildResults.reduce( (probs: object, entry) => {
                const problem = {
                    "type": entry.code ? entry.code : entry.type,
                    "message": entry.message,
                    "isError": ("error".localeCompare(entry.type, undefined, {sensitivity: 'base'}) == 0)
                }
                if(!(entry.source in probs))
                    probs[entry.source] = [];
                probs[entry.source].push(problem);
                return probs;
            }, {} )
            var problemsArr = Object.keys(problemsObj).map( (name) => { return { "name": name, "problems": problemsObj[name] } } )
            setAssetProblemList(problemsArr);
        }
    }, [buildResults]);

    if( isLoading )
        return <LoaderInline />;

    if(buildResults && buildResults.length > 0)
        return (
            <div>
                {xlsxFilePath && <div className={styles.resultTabHeader}><Link href={xlsxFilePath}>Download as XLSX file</Link></div>}
                <Assets assets={assetProblemList} />
            </div>
        );

    return null;    // no data == no display
}

export default AssetReport
