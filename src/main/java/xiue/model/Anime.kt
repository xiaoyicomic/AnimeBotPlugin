package xiue.model

import java.lang.NumberFormatException

data class Anime(val name: String) {
    var id = 0//相应网站的ID
    var img: String? = null//封面直链
    var nums = 0//总集数
    var curNum = 0//当前更新到第几集
    var finish = false//是否已经完结
    var date: String? = null//开始连载时间
    var year: String? = null//年代
    var time: String? = null//更新时间
    var dayOfWeek: Int? = null//星期几更新
    var area: String? = null//地区
    val categories: MutableList<String> = mutableListOf()//类型
    var info: String? = null//简介
    val time2: String?
        get() = time?.run {
            substring(indexOf("每周") + 3)//每周几给它盘了
        }
    val hour: Int
        get() = time2?.let {
            try {
                it.substring(0, it.indexOf(":")).toInt()
            } catch (ee: NumberFormatException) {
                -1
            }
        } ?: -1
    val minute: Int
        get() = time2?.let {
            try {
                it.substring(it.indexOf(":") + 1).toInt()
            } catch (e: NumberFormatException) {
                -1
            }
        } ?: -1

    fun addCategory(category: String) {
        if (!categories.contains(category))
            categories.add(category)
    }
}