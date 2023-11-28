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

package com.buildfifteen.teamcity.uebuildgraph

class FilePathStripper(rootPath: String) {

    private val mRootPath = rootPath.lowercase()
    private val mRootLength = rootPath.length

    fun RemoveRoot(path: String) : String {
        // if the path starts with the Root Path, remove it:
        if(path.lowercase().startsWith(mRootPath))
            return path.drop(mRootLength)
        return path
    }
}