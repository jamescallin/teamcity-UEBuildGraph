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

import com.google.gson.Gson
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


open class BuildGraphMessageStore {
    protected val kMAX_CELL_SIZE = 32000
    protected val mMessages = mutableMapOf<String, BuildGraphMessage>()

    open fun addMessage(message: BuildGraphMessage): Boolean {
        val md5 = message.getMD5()
        return if (mMessages[md5] == null) {
            mMessages[md5] = message
            true
        }
        else {
            val existingMessage = mMessages.getValue(md5)
            existingMessage.occurrences = existingMessage.occurrences + 1
            false
        }
    }

    protected fun getBlocks() : List<String> {
        return mMessages.values.map { it.block }.distinct()
    }

    fun getSummary(block: String?) : BuildGraphLogSummary {
        val list = if(block.isNullOrEmpty()) mMessages.values else mMessages.values.filter { it.block==block }
        val summary = BuildGraphLogSummary(if (block.isNullOrEmpty()) "Summary" else block)

        val errorList = list.filter { it.type=="Error" }
        summary.numErrorsUnique = errorList.size
        summary.numErrors = errorList.sumOf { it.occurrences }

        val warningList = list.filter { it.type=="Warning" }
        summary.numWarningsUnique = warningList.size
        summary.numWarnings = warningList.sumOf { it.occurrences }
        return summary
    }

    fun hasMessages(): Boolean {
        return mMessages.isNotEmpty()
    }

    fun exportJSON(filename: String) {
        if(mMessages.isEmpty())
            return
        val outputFile = File(filename)
        outputFile.writeText(Gson().toJson(mMessages.values))
    }

    open fun exportXLSX(filename: String) {
        if(mMessages.isEmpty())
            return

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()

        var rowNumber = 0
        val header = sheet.createRow(rowNumber)
        header.createCell(0).setCellValue("Occurrences")
        header.createCell(1).setCellValue("Block")
        header.createCell(2).setCellValue("Type")
        header.createCell(3).setCellValue("Source")
        header.createCell(4).setCellValue("Code")
        header.createCell(5).setCellValue("Message")
        rowNumber += 1

        for((_, message) in mMessages ) {
            val row = sheet.createRow(rowNumber)
            val outputMessage = if(message.intro.isNotBlank()) String.format("%s\n%s", message.intro, message.message) else message.message
            row.createCell(0).setCellValue(message.occurrences.toDouble())
            row.createCell(1).setCellValue(message.block)
            row.createCell(2).setCellValue(message.type)
            row.createCell(3).setCellValue(message.source)
            row.createCell(4).setCellValue(message.code)
            row.createCell(5).setCellValue(outputMessage.take(kMAX_CELL_SIZE))
            rowNumber += 1
        }
        sheet.setAutoFilter( CellRangeAddress(0, rowNumber-1, 0, 5) )
        sheet.createFreezePane(0, 1)

        //Write file:
        val outputStream = FileOutputStream(filename)
        workbook.write(outputStream)
        workbook.close()
    }

    fun exportSummary(filename: String) {
        if(mMessages.isEmpty())
            return

        val blocks = getBlocks()
        val summaries = mutableListOf<BuildGraphLogSummary>()

        for( block in blocks ) {
            summaries.add(getSummary(block))
        }

        //Write file:
        val outputFile = File(filename)
        outputFile.writeText(Gson().toJson(summaries))
    }

    companion object {
        fun exportCombinedSummary(filename: String, storeList: Array<BuildGraphMessageStore>) {
            if( storeList.any { it.hasMessages() } ) {
                val summaries = mutableListOf<BuildGraphLogSummary>()

                for(store in storeList ) {
                    val blocks = store.getBlocks()
                    for( block in blocks ) {
                        summaries.add(store.getSummary(block))
                    }
                }
                val outputFile = File(filename)
                outputFile.writeText(Gson().toJson(summaries))
            }
        }
    }
}