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
        val zhTW: String,
        val enUS: String,
        val title: String,
        val _id: String,
        val zhCN: String,
        val owners: List<Owner>,
        val updateTime: String,
        val createTime: String,
        val __v: Int
) {
    var tempKey: MutableList<String> ? = null
}

data class Owner(
        val _id: String,
        val project: String,
        val key: String
)