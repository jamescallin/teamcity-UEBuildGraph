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
import moment from 'moment';

import CookProgressGraph from './CookProgressGraph';
import CookDiagnosticsGraph from "./CookDiagnosticsGraph";
import styles from './CookStats.css'

const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
        return (
            <div className={styles.tooltip}>
                <p>{`Time: ${moment(label).format("hh:mm:ss:SSS")}`}</p>
                {   payload.map( (e) => (<p>{`${e.name}: ${e.value}`}</p>)) }
            </div>
        );
    }
    return null;
};

const tickFormatter = (unixTimestamp) => moment(unixTimestamp).format("hh:mm:ss")

function CookStatsSeq({statSequences}) {
    return (
        <div>
            <CookProgressGraph data={statSequences.progress} tickFormatter={tickFormatter} tooltip={<CustomTooltip />} />
            <CookDiagnosticsGraph data={statSequences.diagnostics} tickFormatter={tickFormatter} tooltip={<CustomTooltip/>} vmMax={statSequences.vmMax} ofhMax={statSequences.ofhMax} />
        </div>
    );
}

export default CookStatsSeq;