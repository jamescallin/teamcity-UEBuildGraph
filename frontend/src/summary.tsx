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

import {Plugin, React} from "@jetbrains/teamcity-api";
import BuildSummary from "./BuildSummary/BuildSummary";

new Plugin(
    [
        Plugin.placeIds.SAKURA_BUILD_LINE_EXPANDED,
    ],
    {
        name: "UEBuildGraph - Results Overview",
        content: BuildSummary,
        options: {
            debug: true,
        }
    }
);

new Plugin(
    [
        Plugin.placeIds.SAKURA_BUILD_OVERVIEW,
        'BUILD_RESULTS_FRAGMENT',
    ],
    {
        name: "UEBuildGraph - Results Overview",
        content: BuildSummary,
        options: {
            debug: true,
        }
    }
);
