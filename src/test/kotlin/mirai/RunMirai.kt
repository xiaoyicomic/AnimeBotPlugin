package mirai

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.pure.MiraiConsolePureLoader
import java.io.File
import java.lang.Exception

object RunMirai {

    // 执行 gradle task: runMiraiConsole 来自动编译, shadow, 复制, 并启动 pure console.

    @JvmStatic
    fun main(args: Array<String>) {
        // 默认在 /test 目录下运行
        try {
            MiraiConsolePureLoader.load("0.39.1","0.4.10") // 启动 console
        }catch (e:Exception){
            e.printStackTrace()
            return
        }
        runBlocking { CommandManager.join() } // 阻止主线程退出
    }
}