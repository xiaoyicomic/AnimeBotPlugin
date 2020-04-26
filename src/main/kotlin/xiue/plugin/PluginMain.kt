package xiue.plugin

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugins.ConfigSection
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.console.utils.getBotManagers
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import xiue.model.Anime
import xiue.utils.AnimeHelper
import xiue.utils.Command
import xiue.utils.DateUtil
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

object PluginMain : PluginBase() {
    private val WEEK_DAYS = listOf("一", "二", "三", "四", "五", "六", "七")//打个表来切换大小写数字(为了周几的显示)

    private val config = loadConfig(FILE_CONFIG)
    val HELP_TRIGGER by config.withDefaultWriteSave { "帮助文档" }
    val YESTERDAY_ANIMES_TRIGGER by config.withDefaultWriteSave { "昨日更新" }
    val TODAY_ANIMES_TRIGGER by config.withDefaultWriteSave { "今日更新" }//今天更新的动漫
    val TOMORROW_ANIMES_TRIGGER by config.withDefaultWriteSave { "明日更新" }
    val DAY_ANIMES_TRIGGER by config.withDefaultWriteSave { "更新查询" }//查询本周X更新的动漫
    val ANIME_DETAIL_TRIGGER by config.withDefaultWriteSave { "动漫查询" }//查询动漫详情
    val LIST_ANIME_SUBSCRIBE_TRIGGER by config.withDefaultWriteSave { "订阅列表" }//查询订阅了什么

    //需要监听消息的群组
    val subscribeGroups by lazy {
        config.setIfAbsent(KEY_NEED_SUBSCRIBING_GROUPS, listOf<Long>())
        config.getLongList(KEY_NEED_SUBSCRIBING_GROUPS).toMutableList()
    }

    //需要推送的动漫
    val subscribeAnimes by lazy {
        config.setIfAbsent(KEY_SUBSCRIBE_ANIMES, listOf<ConfigSection>())
        (config.getConfigSectionList(KEY_NEED_SUBSCRIBING_GROUPS)).toMutableList()
    }

    private val animeHelper = AnimeHelper()
    private val animeTimer = Timer()

    override fun onLoad() {
        super.onLoad()
        logger.info("AnimeBot成功加载")
    }

    override fun onEnable() {
        super.onEnable()
        registerCommands()
        logger.info("AnimeBot已启用")
        subscribeAlways<GroupMessage> {
            if (isSubscribingGroup(this.group.id)) {//监听指定的群聊
                val command = Command(this.message.contentToString())
                command.moveToFirst()
                when (command.current()) {
                    HELP_TRIGGER -> {//发送帮助文档
                        reply(NORMAL_HELP_TEXT)
                    }
                    LIST_ANIME_SUBSCRIBE_TRIGGER -> {//获取订阅列表
                        if (subscribeAnimes.size <= 0) {
                            quoteReply("没有订阅的动漫哦~")
                            return@subscribeAlways
                        }
                        val builder = StringBuilder()
                        var count = 0
                        subscribeAnimes.forEach {
                            configSection2Anime(it)?.apply {
                                builder.append("${count}.$name[$id] 更新时间:$time")
                                    .append("\n")
                            }
                            count++
                        }
                        if (builder.toString() != "")
                            quoteReply("订阅了以下动漫:\n$builder")
                        else
                            quoteReply("没有获取到结果QWQ(可能遇到问题了)")
                    }
                    DAY_ANIMES_TRIGGER -> {//格式: ${DAY_ANIMES_TRIGGER} D (D为本周的第几天，1-7，从周一开始)
                        if (!checkParams(command, 2))
                            return@subscribeAlways
                        val dayOfWeek = try {
                            command.nextInt()
                        } catch (e: NumberFormatException) {
                            quoteReply("最后的参数不是整数XD(需要满足是整数且属于[1,7])")
                        } as Int
                        if (dayOfWeek > 7 || dayOfWeek < 1)
                            quoteReply("你欺负我！哪里来的星期${dayOfWeek}，哼！")
                        else
                            PluginMain.launch {
                                animeHelper.getDayAnimes(dayOfWeek)?.let {
                                    quoteReply("本周${WEEK_DAYS[dayOfWeek - 1]}更新的动漫:\n${it.makeAnimesCalendarStr()}")
                                } ?: quoteReply("获取失败嘞QAQ")
                            }
                    }
                    YESTERDAY_ANIMES_TRIGGER -> {
                        PluginMain.launch {
                            val dayOfWeek = DateUtil.getDayOfWeek() - 1;
                            animeHelper.getDayAnimes(dayOfWeek)?.let {
                                quoteReply("昨天更新的动漫:\n${it.makeAnimesCalendarStr()}")
                            } ?: quoteReply("获取失败嘞QAQ")
                        }
                    }
                    TODAY_ANIMES_TRIGGER -> {
                        if (animeHelper.isLatestTodayAnimes) {
                            quoteReply(animeHelper.todayAnimes.makeTodayAnimesMessage())
                        } else
                            PluginMain.launch {
                                if (animeHelper.loadTodayAnimes())
                                    quoteReply(animeHelper.todayAnimes.makeTodayAnimesMessage())
                                else
                                    quoteReply("貌似获取失败嘞QWQ")
                            }
                    }
                    TOMORROW_ANIMES_TRIGGER -> {
                        PluginMain.launch {
                            val dayOfWeek = DateUtil.getDayOfWeek() + 1;
                            animeHelper.getDayAnimes(dayOfWeek)?.let {
                                quoteReply("明天更新的动漫:\n${it.makeAnimesCalendarStr()}")
                            } ?: quoteReply("获取失败嘞QAQ")
                        }
                    }
                    ANIME_DETAIL_TRIGGER -> {//格式: 动漫查询 ID
                        if (!checkParams(command, 2))
                            return@subscribeAlways
                        val id = try {
                            command.nextInt()
                        } catch (e: NumberFormatException) {
                            quoteReply("ID格式不对哦")
                            return@subscribeAlways
                        }
                        try {
                            val anime: Anime? = animeHelper.getAnimeDetail(id)
                            anime?.let {
                                quoteReply(makeDetailMessage(it))
                            } ?: quoteReply("貌似获取失败嘞~QAQ~")
                        } catch (e: Exception) {
                            quoteReply("遇到错误了？！！(${e.message})")
                            return@subscribeAlways
                        }
                    }
                }
            }
        }
        //TODO 进行订阅任务
    }

    override fun onDisable() {
        super.onDisable()
        save()
    }


    /**
     * 保存配置等信息
     */
    private fun save() {
        config["Need_Subscribing_Groups"] = subscribeGroups
        config["Need_Subscribing_Animes"] = subscribeAnimes
        config.save()
    }

    private suspend fun GroupMessage.checkParams(command: Command, length: Int): Boolean {
        if (command.length() != length) {
            quoteReply("格式怎么感觉不对哩？")
            return false
        }
        return true
    }

    private suspend fun GroupMessage.makeDetailMessage(anime: Anime): Message {
        return buildMessageChain {
            +this@makeDetailMessage.uploadImage(image = URL(anime.img))
            +"${anime.name}(${anime.id})[${anime.nums}集]\n"
            +"年代:${anime.year} 地区:${anime.area}\n"
            +"状态:${if (anime.finish) "已完结" else "连载中"}\n"
            if (anime.categories.size > 0) {//添加标签
                "类型: ".unaryPlus()
                anime.categories.forEach {
                    "$it ".unaryPlus()
                }
                "\n".unaryPlus()
            }
            if (!anime.finish)
                anime.time?.let {
                    "更新时间:${anime.time}\n".unaryPlus()
                }
            +"简介:${anime.info}"
        }
    }

    private fun List<Anime>.makeTodayAnimesMessage(): String {
        return "今日更新:\n${makeAnimesCalendarStr()}"
    }

    private fun List<Anime>.makeAnimesCalendarStr(): String {
        val builder = StringBuilder()
        var num = 1
        forEach {
            builder.append("$num.${it.name}[${it.id}]更新至第${it.curNum}集")
                .append("\n")
            num++
        }
        return builder.toString()
    }
}
