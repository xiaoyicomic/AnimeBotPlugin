package xiue.plugin

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ContactCommandSender
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.ConfigSection
import net.mamoe.mirai.contact.Group
import xiue.model.Anime
import java.lang.NumberFormatException

fun findAnimeByID(list: List<ConfigSection>, id: Int): Anime? {
    val configSection = list.find {
        return@find it["id"] == id
    } ?: return null
    return configSection2Anime(configSection)
}

fun configSection2Anime(configSection: ConfigSection): Anime? {
    return try {
        Anime(configSection.getString("name")).apply {
            id = configSection.getInt("id")
            dayOfWeek = configSection.getInt("day_of_week")
            time = "每周$dayOfWeek$${configSection.getString("time")}"
        }
    } catch (e: NoSuchElementException) {
        null
    }
}

fun anime2ConfigSection(anime: Anime): ConfigSection {
    val configSection = ConfigSection.new()
    configSection["name"] = anime.name
    configSection["id"] = anime.id
    configSection["time"] = anime.time2 ?: ""
    configSection["day_of_week"] = anime.dayOfWeek?.toString() ?: 0
    return configSection
}

fun PluginMain.removeSubscribeAnime(anime: Anime) {
    subscribeAnimes.remove(anime2ConfigSection(anime))
}

fun PluginMain.addSubscribeAnime(anime: Anime) {
    if (findAnimeByID(subscribeAnimes, anime.id) == null)//防止重复添加
        subscribeAnimes.add(anime2ConfigSection(anime))
}

fun PluginMain.addSubscribingGroup(id: Long) {
    if (!subscribeGroups.contains(id))
        subscribeGroups.add(id)
}

fun PluginMain.removeSubscribingGroup(id: Long) {
    subscribeGroups.remove(id)
}

fun PluginMain.isSubscribingGroup(id: Long): Boolean {
    return subscribeGroups.contains(id)
}

fun PluginMain.registerCommands() {
    registerCommand {
        name = "anime"
        description = "AnimeBot插件指令介绍"
        alias = listOf("anime")
        usage = """
            /anime
              [add group QQ群号(或者this代替本群)]：监听一个群聊
              [remove group QQ群号(或者this代替本群)]：不再监听某个群聊
              [add anime 动漫名字 樱花动漫对应的ID 更新时间(00:00格式) 星期几(写个整数就行)]：订阅一个动漫的更新
              [remove anime 樱花动漫对应的ID]：取消订阅某个动漫
              [help]：获取帮助
            """.trimIndent()//TODO 补全指令介绍
        onCommand {
            if (it.isEmpty())
                return@onCommand false
            //TODO 实现动漫订阅添加、删除
            when (it[0]) {
                //添加格式 add 组 值
                "add" ->
                    when (it[1]) {
                        "anime" -> {//add anime 名字 ID 时间 星期几(整数)
                            if (it.size != 6) {
                                sendErrorParamsMessage(it)
                                return@onCommand false
                            }
                            val anime = Anime(it[2]).apply {
                                id = try {
                                    it[3].toInt()
                                } catch (e: NumberFormatException) {
                                    sendMessage("ID不是整数:${it[3]}")
                                    return@onCommand false
                                }
                                dayOfWeek = try {
                                    it[5].toInt()
                                } catch (e: NumberFormatException) {
                                    sendMessage("星期几不是整数:${it[5]}")
                                    return@onCommand false
                                }
                                if (dayOfWeek!! < 1 || dayOfWeek!! > 7) {
                                    sendMessage("为啥还有星期${dayOfWeek}")
                                    return@onCommand false
                                }
                                time = "每周${dayOfWeek}$it[4]"
                            }
                            addSubscribeAnime(anime)
                            sendMessage("动漫[${anime.id}]订阅成功")
                            return@onCommand true
                        }
                        "group" -> {//add group QQ
                            if (it.size != 3) {
                                sendErrorParamsMessage(it)
                                return@onCommand false
                            }
                            val group: Long;
                            if (it[2] == "this") {
                                group = getCurrentGroupID()
                            } else {
                                try {
                                    group = it[2].toLong()
                                } catch (e: Exception) {
                                    sendMessage("最后一位参数不是整数或者this:${it[2]}")
                                    return@onCommand false
                                }
                            }
                            if (group != 0L) {
                                addSubscribingGroup(group)
                                sendMessage("群聊${group}已经加入监听")
                                return@onCommand true
                            } else {
                                sendMessage("参数错误:${it[2]}")
                                return@onCommand false
                            }
                        }
                        else -> sendMessage("无法找到该组:${it[1]}")
                    }
                //remove 组 值
                "remove" ->
                    when (it[1]) {
                        "anime" -> {//remove anime id
                            if (it.size != 3) {
                                sendErrorParamsMessage(it)
                                return@onCommand false
                            }
                            val id = try {
                                it[2].toInt()
                            } catch (e: NumberFormatException) {
                                sendMessage("ID不是整数:${it[2]}")
                                return@onCommand false
                            }
                            findAnimeByID(subscribeAnimes, id)?.let {
                                removeSubscribeAnime(it)
                            } ?: run {
                                sendMessage("订阅列表里找不到这个动漫QAQ")
                                return@onCommand false
                            }
                        }
                        "group" -> {//remove group id
                            if (it.size != 3) {
                                sendErrorParamsMessage(it)
                                return@onCommand false
                            }
                            val group = if (it[2] == "this") {
                                getCurrentGroupID()
                            } else {
                                try {
                                    it[2].toLong()
                                } catch (e: Exception) {
                                    sendMessage("最后一位参数不是整数或者this:${it[2]}")
                                    return@onCommand false
                                }
                            }
                            if (group != 0L) {
                                removeSubscribingGroup(group)
                                sendMessage("群聊${group}已经移除监听")
                                return@onCommand true
                            } else {
                                return@onCommand false
                            }
                        }
                        else -> sendMessage("无法找到该组:${it[1]}")
                    }
                "help" -> {
                    sendMessage(usage)
                    return@onCommand true
                }
                //匹配不到参数
                else -> return@onCommand false
            }
            return@onCommand false
        }
    }
}

suspend fun CommandSender.sendErrorParamsMessage(it: List<String>) {
    sendMessage("参数错误:" +
            it.run {
                val builder = StringBuilder()
                forEach {
                    builder.append(it).append(" ")
                }
                builder.toString()
            }
    )
}

fun CommandSender.getCurrentGroupID(): Long {
    if (this is ContactCommandSender && contact is Group) {
        return (contact as Group).id
    }
    return 0;
}