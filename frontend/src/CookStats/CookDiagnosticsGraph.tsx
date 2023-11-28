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
import { XAxis, YAxis, CartesianGrid, Tooltip, Legend, Line, LineChart, ResponsiveContainer } from 'recharts';

import styles from './CookStats.css'

function CookDiagnosticsGraph({data, tooltip, tickFormatter, ofhMax, vmMax}) {
    return (
        <div>
            <ResponsiveContainer className={styles.cookStatsSeqContainer} width="99%" height={300}>
                <LineChart
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
                    <CartesianGrid strokeDasharray="3 3"/>
                    <XAxis dataKey="time"
                           type="number"
                           domain={['dataMin', 'dataMax']}
                           tickFormatter={tickFormatter}/>
                    <YAxis yAxisId="left" type="number" domain={[0, ofhMax]}/>
                    <YAxis yAxisId="right" orientation="right" type="number" domain={[0, vmMax]}/>
                    <Tooltip content={tooltip}/>
                    <Legend/>
                    <Line yAxisId="left" type="monotone" dataKey="OpenFileHandles" stroke="#8884d8" activeDot={{r: 8}}/>
                    <Line yAxisId="right" type="monotone" dataKey="VirtualMemory" stroke="#82ca9d"/>
                </LineChart>
            </ResponsiveContainer>
        </div>
    );

}

export default CookDiagnosticsGraph;
