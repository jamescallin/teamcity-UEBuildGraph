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

import {React} from "@jetbrains/teamcity-api"
import {H3} from '@jetbrains/ring-ui/components/heading/heading'

import styles from './CookStats.css'

const Cell = ({indent, children}) => (
    <div className={`${styles.col} ${indent}`}>{children}</div>
);

const getIndentStyle = (level) => {
    switch(level) {
        case 0:
            return styles.uebgHier0;
        case 1:
            return styles.uebgHier1;
        case 2:
            return styles.uebgHier2;
        case 3:
            return styles.uebgHier3;
        case 4:
            return styles.uebgHier4;
        case 5:
            return styles.uebgHier5;
    }
}

function CookStatsHierarchical({statHierarchy}) {
    return (
        <div className={styles.cookStatsTable}>
            {   Object.entries(statHierarchy).map(([section, stats]) => (
                <React.Fragment>
                    <header><Cell><H3>{section}</H3></Cell><Cell /></header>
                    { stats.map( (stat) => (
                        <div>
                            <Cell indent={getIndentStyle(stat.level)}>{stat.value}</Cell>
                            <Cell>{stat.name}</Cell>
                        </div>
                    ) )}
                </React.Fragment>
            ))
            }
        </div>
    );
}

export default CookStatsHierarchical;
