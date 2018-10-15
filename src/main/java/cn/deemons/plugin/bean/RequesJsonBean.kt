package cn.deemons.plugin.bean


data class RequesJsonBean(
        val list: List<X>,
        val project: String = "app-android"
)

data class X(
        val key: String,
        val zhCN: String,
        val enUS: String,
        val zhTW: String
)