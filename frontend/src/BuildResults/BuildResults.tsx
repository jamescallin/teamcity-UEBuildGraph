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
import Pager from "@jetbrains/ring-ui/components/pager/pager";
import Selection, {SelectionItem} from "@jetbrains/ring-ui/components/table/selection";
import {SortParams} from "@jetbrains/ring-ui/components/table/header-cell";
import Table from "@jetbrains/ring-ui/components/table/table";

import styles from './BuildResults.css'
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

function BuildResults({buildid}) {
    const [isLoading, setIsLoading] = React.useState(true);
    const [buildResults, setBuildResults] = React.useState<Array<BuildResults>>(EmptyArray);
    const [tableData, setTableData] = React.useState<Array<BuildResults>>(EmptyArray);
    const [sortKey, setSortKey] = React.useState('occurrences');
    const [sortOrder, setSortOrder] = React.useState(false);
    const [page, setPage] = React.useState(1);
    const [pageSize, setPageSize] = React.useState(20);
    const [xlsxFilePath, setXlsxFilePath] = React.useState("");

    const onSort = (event: SortParams) => {
        setSortKey(event.column.id);
        setSortOrder(event.order);
    };

    React.useEffect( () => {
        const requestResults = async () => {
            try {
                const result: Array<BuildResults> = await utils.requestJSON(`app/rest/builds/id:${buildid}/artifacts/content/uebuildgraph/build-report.json`);
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
                const xlsxFile = result['file'].find((file) => file.name.startsWith("build") && file.name.endsWith("xlsx") );
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
        let sortedResults = buildResults;
        sortedResults.sort((a, b) => String(a[sortKey]).localeCompare(String(b[sortKey])) * (sortOrder ? 1 : -1));
        sortedResults = sortedResults.slice((page - 1) * pageSize, (page - 1) * pageSize + pageSize);
        setTableData(sortedResults);
    }, [buildResults, sortKey, sortOrder, page, pageSize]);

    if( isLoading )
        return <LoaderInline />;

    if(buildResults && buildResults.length > 0)
        return (
            <div>
                {xlsxFilePath && <div className={styles.resultTabHeader}><Link href={xlsxFilePath}>Download as XLSX file</Link></div>}
                <Table data={tableData}
                       columns={[
                            { id:"occurrences", title:"Count",   sortable : true },
                            { id:"block",       title:"Block",   sortable : true },
                            { id:"source",      title:"Source",  sortable : true },
                            { id:"type",        title:"Type",    sortable : true },
                            { id:"code",        title:"Code",    sortable : true },
                            { id:"message",     title:"Message", sortable : true },
                       ]}
                       selection={new Selection}
                       onSort={onSort}
                       sortKey={sortKey}
                       sortOrder={sortOrder}
                       selectable={false}
                       autofocus={false}
                       draggable={false}
                       getItemClassName={() => styles.resultItem}
                />
                <Pager
                    total={buildResults.length}
                    pageSize={pageSize}
                    currentPage={page}
                    onPageChange={(page: number) => setPage(page)}
                    onPageSizeChange={(ps: number) => setPageSize(ps)}
                />
            </div>
        );

    return null;    // no data == no display
}

export default BuildResults
