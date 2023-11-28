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

import jetbrains.buildServer.serverSide.crypt.EncryptUtil.md5

data class BuildGraphMessage(val block: String, val source: String, val type: String, val code: String, var message: String, var intro: String, var indent: Int ) {
    var occurrences: Int = 1

    override fun toString(): String {
        val codeStr = if(code.isNotBlank()) "${code}: " else ""
        val introStr = if(intro.isNotBlank()) "${intro}\n" else ""
        val indentation = " ".repeat(indent)
        return "${introStr}${indentation}${source}: ${type}: ${codeStr}${message}"
    }

    fun getMD5() : String {
        return md5( this.toString() )
    }
}
