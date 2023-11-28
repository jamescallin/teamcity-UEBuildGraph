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
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

import styles from './CookStats.css'

function CookProgressGraph({data, tooltip, tickFormatter}) {
    return (
        <div>
            <ResponsiveContainer className={styles.cookStatsSeqContainer} width="99%" height={300}>
                <AreaChart
                    width={500}
                    height={300}
                    data={data}
                    margin={{
                        top: 20,
                        right: 30,
                        left: 20,
                        bottom: 5,
                    }}
                >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="time"
                           type="number"
                           domain={['dataMin', 'dataMax']}
                           tickFormatter={tickFormatter}/>
                    <YAxis yAxisId="left" />
                    <YAxis yAxisId="right" orientation="right" />
                    <Tooltip content={tooltip} />
                    <Legend />
                    <Area yAxisId="left" type="monotone" dataKey="cooked" stackId="a" fill="#8884d8" />
                    <Area yAxisId="left" type="monotone" dataKey="remain" stackId="a" fill="#82ca9d" />
                </AreaChart>
            </ResponsiveContainer>
        </div>
    );
}

export default CookProgressGraph;
