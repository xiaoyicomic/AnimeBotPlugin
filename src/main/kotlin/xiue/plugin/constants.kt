package xiue.plugin

const val SUPER_ADMIN: Long = 1612108278L //我不管，爷就是超级管理员

//文件名
const val FILE_CONFIG = "config.yml"

//记录一些配置文件用的KEY
const val KEY_NEED_SUBSCRIBING_GROUPS = "Need_Subscribing_Groups"

//记录需要订阅的动漫
const val KEY_SUBSCRIBE_ANIMES = "Key_Subscribe_Animes"

object Bangumi {
    @JvmStatic
    val BASE_URL = "https://api.bgm.tv/"

    @JvmStatic
    val CALENDAR_URL = "$BASE_URL/calendar"
}

//樱花动漫
object Sakura {
    @JvmStatic
    val BASE_URL = "http://www.imomoe.in/"

    @JvmStatic
    val CALENDAR_URL = BASE_URL

    @JvmStatic
    val DETAIL_URL_MODEL = "${BASE_URL}view/%d.html"

    @JvmStatic
    fun getIDFromRelativeURL(url: String): Int {
        return url.run {
            substring(
                indexOf("/view/") + "/view/".length,
                indexOf(".")
            )
        }.toInt()
    }

    @JvmStatic
    fun getDetailURLByID(id: Int): String {
        return DETAIL_URL_MODEL.format(id)
    }

    @JvmStatic
    fun getURL(relativeURL: String): String {
        return BASE_URL + relativeURL
    }
}

//帮助文档(给群员看的)
internal val PluginMain.NORMAL_HELP_TEXT: String
    get() = """
        需知：本插件功能定位在动漫方向，具有动漫查询、订阅、更新列表查看等功能，数据来源于樱花动漫(嘶哩嘶哩、Bangumi、风车动漫可能会添加，但是目前没有用到)。
        注意：一些动漫名字后面可能会有[]，[]里面的数字就是对应的资源ID
        一、普通用户拥有以下命令：
        1.${TODAY_ANIMES_TRIGGER} -> 查询今天要更新的动漫 (同理还有${YESTERDAY_ANIMES_TRIGGER}、${TOMORROW_ANIMES_TRIGGER})
        2.${DAY_ANIMES_TRIGGER} 星期几(输入一个整数，1-7，代表星期一至星期日) ->查询本周某一天的更新列表
        3.${ANIME_DETAIL_TRIGGER} 动漫ID -> 获取某个动漫的详情
        4.${LIST_ANIME_SUBSCRIBE_TRIGGER} -> 查询所有订阅的动漫
        5.${HELP_TRIGGER} 获取帮助文档
        二、管理员命令请使用该命令查询:/anime help
    """.trimIndent()