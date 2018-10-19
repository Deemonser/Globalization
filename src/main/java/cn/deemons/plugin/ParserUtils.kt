package cn.deemons.plugin

import cn.deemons.plugin.bean.LanguageBean
import cn.deemons.plugin.bean.LanguageBean_X
import cn.deemons.plugin.bean.RequesJsonBean
import cn.deemons.plugin.bean.X
import com.google.gson.Gson
import jxl.Cell
import jxl.Workbook
import jxl.Workbook.createWorkbook
import jxl.write.Label
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileWriter

object ParserUtils {

    val fileName = "strings_t.xml"
    val excelName = "translation.xls"
    val JsonName = "translation.json"


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

        val workbook = createWorkbook(excelFile)
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

    fun getJsonFile(srcFile: File): File {
        val file = File(srcFile.absolutePath + File.separator + JsonName)
        if (!file.exists()) file.createNewFile()
        return file
    }

    fun parseTableToJson(projectFile: File, isInitPush: Boolean = false, onListener: (string: String) -> Unit) {

        onListener("=========== parseTable -->  Json ============")

        val excelFile = getExcelFile(projectFile)
        if (!projectFile.exists() || !excelFile.exists()) return

        val workbook = Workbook.getWorkbook(excelFile) ?: return


        val list = mutableListOf<X>()

        workbook.sheets.forEach {
            for (i in 1 until it.rows) {
                val key = it.getCell(0, i).contents
                val cn = it.getCell(1, i).contents

                var us = ""
                var tw = ""

                if (isInitPush) {
                    try {
                        us = it.getCell(2, i).contents
                        tw = it.getCell(3, i).contents
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        e.printStackTrace()
                    }
                }

                System.out.println("i=$i ,cn=$cn , us=$us")
                list.add(X(key, cn, us, tw))
            }
        }

        val jsonBean = RequesJsonBean(list)

        mergeData(jsonBean, onListener)
    }


    fun parseJsonToTable(file: File, languageBean: LanguageBean, onListener: (string: String) -> Unit) {

        onListener("=========== parseJson --> Table ============")

        val excelFile = getExcelFile(file)
        if (!file.exists()) return
        val copyFile = File(excelFile.parent + "/copy.xls")

        if (copyFile.exists()) copyFile.delete()
        copyFile.createNewFile()

        val workbook = Workbook.getWorkbook(excelFile)

        val copyWorkbook = createWorkbook(copyFile)

        var languageList = mutableListOf<LanguageBean_X>()
        languageBean.data.list.forEach { if (containAndroidKey(it)) languageList.add(it) }


        var count = 0

        workbook.sheets.forEachIndexed { index, sheet ->

            val copySheet = copyWorkbook.createSheet(sheet.name, index)

            for (i in 0 until sheet.columns) {
                copySheet.addCell(Label(i, 0, sheet.getCell(i, 0).contents))

            }

            val columns = sheet.columns

            for (i in 1 until sheet.rows) {
                val key = sheet.getCell(0, i).contents
                val cn = sheet.getCell(1, i).contents
                var us = if (columns > 2) sheet.getCell(2, i).contents else ""
                var tw = if (columns > 3) sheet.getCell(3, i).contents else ""
                var type = "原文"

                val first = languageList.firstOrNull { it.tempKey != null && it.tempKey!!.contains(key) && it.zhCN == cn }
                if (first != null && (us != first.enUS || tw != first.zhTW)) {
                    type = "替换"
                    us = first.enUS
                    tw = first.zhTW
                }

                copySheet.addCell(Label(0, i, key))
                copySheet.addCell(Label(1, i, cn))
                copySheet.addCell(Label(2, i, us))
                copySheet.addCell(Label(3, i, tw))
                count++
                System.out.println("type=$type ,count=$count ,i=$i ,key=$key ,cn=$cn ,us=$us ,tw=$tw")
                onListener("type=$type ,count=$count ,i=$i ,key=$key ,cn=$cn , us=$us ,tw=$tw")

            }
        }

        copyWorkbook.write()
        copyWorkbook.close()

        excelFile.delete()
        copyFile.renameTo(excelFile)

    }

    private fun containAndroidKey(bean_X: LanguageBean_X): Boolean {
        bean_X.owners.forEach {
            if (it.project == "app-android") {
                if (bean_X.tempKey == null) bean_X.tempKey = mutableListOf()
                bean_X.tempKey?.add(it.key)
            }
        }
        return bean_X.tempKey != null
    }


    /**
     *  合并云端数据
     */
    fun mergeData(requesJsonBean: RequesJsonBean, onListener: (string: String) -> Unit) {

        onListener("=========== Upload Json ============")

        val url = "http://alibetalanguagemanage.followme-inc.com/app/list/merge"


        val requestJson = Gson().toJson(requesJsonBean)

//        System.out.println(requestJson)

        onListener(requestJson)

        val request = Request.Builder()
                .addHeader("cookie", "token=ZcCHObPMlCrTEUz0")
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson))
                .build()

        val okHttpClient = OkHttpClient()
        val response = okHttpClient.newCall(request).execute()

        onListener(response.toString())
    }


    /**
     *  获取新的翻译数据
     */
    fun getNetData(excelFile: File, onListener: (string: String) -> Unit) {

        onListener("=========== download json -->  table ============")

        val url = "http://alibetalanguagemanage.followme-inc.com/api/list?pageSize=99999"

        val okHttpClient = OkHttpClient()

        val request = Request.Builder()
                .addHeader("cookie", "token=ZcCHObPMlCrTEUz0")
                .url(url)
                .build()


        val response = okHttpClient.newCall(request).execute()

        val result = response.body()?.string()
        System.out.println(result)

        val languageBean = Gson().fromJson<LanguageBean>(result, LanguageBean::class.java)

        System.out.println(languageBean.toString())
        onListener(languageBean.toString())

        parseJsonToTable(excelFile, languageBean, onListener)
    }
}


fun main(args: Array<String>) {
    val file = File("/Users/deemons/Desktop/AndroidProject/FollowmeAndroidWithComponent")
    val excelFile = File("/Users/deemons/Desktop/translation.xls")

    //xml 转成 Excel
//    ParserUtils.parseXmlToTable(file, {})

    // Excel 转 JSon 并上传
//    ParserUtils.parseTableToJson(file, {})

    // 获取云端数据，转换并替换原来的 Excel
    ParserUtils.getNetData(file, {})

    // 将 Excel 转成 xml
    ParserUtils.parseTableToXml(file, {})

//    ParserUtils.deleteFiles(file,{})
    ParserUtils.replaceFiles(file) {}
}


