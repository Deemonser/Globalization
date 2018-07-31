package cn.deemons.plugin

import jxl.Cell
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileWriter

object ParserUtils {

    val fileName = "strings_t.xml"
    val excelName = "translation.xls"


    fun replaceFiles(file: File, onListener: (string: String) -> Unit) {
        if (!file.exists()) return

        onListener("\n")
        onListener("=========== replaceFiles ============")


        if (file.isDirectory) {
            file.listFiles().forEach { replaceFiles(it, onListener) }
        } else if (file.name == fileName) {
            val srcFile = File(file.parent + File.separator + "strings.xml")
            if (srcFile.exists()) {
                srcFile.delete()
            }
            file.renameTo(srcFile)
        }

    }

    fun deleteFiles(file: File, onListener: (string: String) -> Unit) {
        if (!file.exists()) return

        onListener("\n")
        onListener("=========== deleteFiles ============")

        if (file.isDirectory) {
            file.listFiles().forEach { deleteFiles(it, onListener) }
        } else if (file.name == fileName) {
            onListener(file.absolutePath)
            file.delete()
        }
    }


    fun parseTableToXml(projectFile: File, onListener: (string: String) -> Unit) {
        val excelFile = getExcelFile(projectFile)
        if (!projectFile.exists() || !excelFile.exists()) return

        val workbook = Workbook.getWorkbook(excelFile) ?: return

        onListener("\n")
        onListener("=========== parseTable  -->  Xml ============")


        workbook.sheets.forEach {
            val filePath = File(projectFile.absolutePath + File.separator + it.name + "/src/main/res").absolutePath
            for (index in 1 until it.columns) {
                val parenFile = File(filePath + File.separator + it.getCell(index, 0).contents)
                if (!parenFile.exists()) parenFile.createNewFile()
                val file = File(parenFile.absolutePath + File.separator + fileName)
                onListener(file.absolutePath)
                writeXML(file, it.getColumn(0), it.getColumn(index))
            }
        }
    }

    fun writeXML(file: File, keys: Array<out Cell>, values: Array<Cell>) {
        val root = DocumentHelper.createDocument().addElement("resources")
        keys.forEachIndexed { index, it ->
            if (index > 0 && values.size > index) {
                root.addElement("string")
                        .addAttribute("name", it.contents)
                        .addText(values[index].contents)
            }
        }

        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val writer = FileWriter(file)
        root.write(writer)
        writer.close()
    }

    fun parseXmlToTable(file: File, onListener: (string: String) -> Unit) {
        val excelFile = getExcelFile(file)
        if (!file.exists()) return
        onListener("\n")
        onListener("=========== parseXml -->  Table ============")

        if (excelFile.exists()) {
            excelFile.delete()
        }
        excelFile.createNewFile()

        val workbook = Workbook.createWorkbook(excelFile)
        read(file, workbook, onListener)
        workbook.write()
        workbook.close()
    }

    var column = 0
    var tableIndex = 0
    private fun read(file: File, workbook: WritableWorkbook, onListener: (string: String) -> Unit) {

        if (file.isDirectory) {
            file.listFiles().forEach { read(it, workbook, onListener) }
        } else if (file.name == "strings.xml") {
            onListener(file.absolutePath)
            val document = SAXReader().read(file)
            val moduleName = file.parentFile.parentFile.parentFile.parentFile.parentFile.name
            var writableSheet = workbook.getSheet(moduleName)
            if (writableSheet == null) {
                writableSheet = workbook.createSheet(moduleName, tableIndex++)
                column = 0
            }
            val valuesName = file.parentFile.name
            if (valuesName == "values") {
                writableSheet.addCell(Label(column++, 0, "key"))
                writableSheet.addCell(Label(column++, 0, valuesName))
                writeTableFirstColumn(document, writableSheet)

            } else {
                writableSheet.addCell(Label(column, 0, valuesName))
                writeTableColumn(document, writableSheet, column++)
            }
        }

    }


    fun writeTableFirstColumn(document: Document, sheet: WritableSheet) {
        document.rootElement.elements().forEachIndexed { indext, it ->
            sheet.addCell(Label(0, indext + 1, it.attribute("name").value))
            sheet.addCell(Label(1, indext + 1, it.stringValue))

        }

    }

    fun writeTableColumn(document: Document, sheet: WritableSheet, column: Int) {
        document.rootElement.elements().forEachIndexed { _, it ->
            val cell = sheet.findCell(it.attribute("name").value)
            if (cell == null) {
                sheet.addCell(Label(column, sheet.rows, it.stringValue))
            } else {
                sheet.addCell(Label(column, cell.row, it.stringValue))
            }
        }
    }

    fun getExcelFile(srcFile: File): File {
        val file = File(srcFile.absolutePath + File.separator + excelName)
        if (!file.exists()) file.createNewFile()
        return file
    }

}

fun main(args: Array<String>) {
    val file = File("/Users/deemons/Desktop/AndroidProject/FollowmeAndroidWithComponent")
    val excelFile = File("/Users/deemons/Desktop/translation.xls")
//    parseXmlToTable(file, excelFile)

    ParserUtils.parseTableToXml(file, {})

//    deleteFiles(file)
    ParserUtils.replaceFiles(file) {}
}


