package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import me.shetj.router.SRouterKit
import me.shetj.router.startActivity
import me.shetj.service.SModuleServiceKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.startJump1).setOnClickListener {
            SRouterKit.startActivity(this@MainActivity, "activity/router")
        }

        findViewById<View>(R.id.startJump2).setOnClickListener {

            startActivity {
                path = "activity/router2"
                mapInfo = null
                bundle = null
                requestCode = 1
            }

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