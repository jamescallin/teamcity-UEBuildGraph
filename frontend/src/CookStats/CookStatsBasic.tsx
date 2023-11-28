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

const Cell = ({children}) => (
    <div className={styles.col}>{children}</div>
);

function CookStatsBasic({statList}) {
    return (
        <div className={styles.cookStatsTable}>
            {   Object.entries(statList).map(([section, stats]) => (
                <React.Fragment>
                    <header><Cell><H3>{section}</H3></Cell><Cell /></header>
                    {Object.entries(stats).map( ([statname, statvalue]) => (
                        <div>
                            <Cell>{statname}</Cell>
                            <Cell>{statvalue}</Cell>
                        </div>
                    ) )}
                </React.Fragment>
            ))
            }
        </div>
    );
}

export default CookStatsBasic;
