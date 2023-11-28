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

import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

class BuildGraphMessageStoreValidateAsset() : BuildGraphMessageStore() {
    override fun addMessage(message: BuildGraphMessage): Boolean {
        if(message.block == BuildGraphConstants.BLOCK_VALIDATEASSET)
            return super.addMessage(message)
        return false
    }

    override fun exportXLSX(filename: String) {
        if(mMessages.isEmpty())
            return

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()

        var rowNumber = 0
        val header = sheet.createRow(rowNumber)
        header.createCell(0).setCellValue("Occurrences")
        header.createCell(1).setCellValue("Type")
        header.createCell(2).setCellValue("Source")
        header.createCell(3).setCellValue("Test Name")
        header.createCell(4).setCellValue("Message")
        rowNumber += 1

        for((_, message) in mMessages ) {
            val row = sheet.createRow(rowNumber)
            row.createCell(0).setCellValue(message.occurrences.toDouble())
            row.createCell(1).setCellValue(message.type)
            row.createCell(2).setCellValue(message.source)
            row.createCell(3).setCellValue(message.code)
            row.createCell(4).setCellValue(message.message.take(kMAX_CELL_SIZE))
            rowNumber += 1
        }
        sheet.setAutoFilter( CellRangeAddress(0, rowNumber-1, 0, 4) )
        sheet.createFreezePane(0, 1)

        //Write file:
        val outputStream = FileOutputStream(filename)
        workbook.write(outputStream)
        workbook.close()
    }

}