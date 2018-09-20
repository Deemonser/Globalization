package cn.deemons.plugin.bean


data class LanguageBean(
        val code: Int,
        val data: Data
)

data class Data(
        val total: Int,
        val pageSize: Int,
        val pageIndex: Int,
        val list: List<LanguageBean_X>
)

data class LanguageBean_X(
        val projects: List<String>,
        val zhTW: String,
        val enUS: String,
        val title: String,
        val remark: String,
        val _id: String,
        val id: String,
        val zhCN: String,
        val updateTime: String,
        val createTime: String,
        val __v: Int
)