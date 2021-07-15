package me.shetj.routetkit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import me.shetj.router.SRouterKit
import me.shetj.router.startActivity
import me.shetj.service.SModuleServiceKit
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.startJump1).setOnClickListener {
            SRouterKit.startActivity(this@MainActivity, "activity/router")
            findViewById<TextView>(R.id.tv_msg).append("\nactivity/router")
        }

        findViewById<View>(R.id.startJump2).setOnClickListener {
            //测试正常跳转
            startActivity {
                path = "activity/router2"
                requestCode = 1
            }
            findViewById<TextView>(R.id.tv_msg).append("\nactivity/router2")
        }

        findViewById<View>(R.id.startJump3).setOnClickListener {
            //测试正常跳转
            SRouterKit.startActivity("router://activity/scheme/router2?key=1&key2=2")
            findViewById<TextView>(R.id.tv_msg).append("\nrouter://activity/scheme/router2?key=1&key2=2")
        }


        findViewById<View>(R.id.startJump4).setOnClickListener {
            //测试跳转其他APP
//            SRouterKit.startActivity("lihua://webview?url=https://www.baidu.com?st=lzwk_push&inviter_id=33427609")
            val path = "beloved://shell/activity/index"
            SRouterKit.startActivity(path)
            findViewById<TextView>(R.id.tv_msg).append("\n"+path)
        }


        val info = "name = ${
            SModuleServiceKit.getInstance().get<UserService>("defService")?.getName() ?: "为获取到名字"
        }\n" +
                "age = ${
                    SModuleServiceKit.getInstance().get<UserService>("defService")
                        ?.getAge() ?: "为获取到age"
                }\n"
        findViewById<TextView>(R.id.tv_msg).apply {
            text = info
        }
    }
}