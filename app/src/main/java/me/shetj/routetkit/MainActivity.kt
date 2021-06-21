package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import me.shetj.router.SRouterKit
import me.shetj.service.SModuleServiceKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.startJump1).setOnClickListener {
            SRouterKit.startActivity(this@MainActivity, "activity/router")
        }
        findViewById<View>(R.id.startJump2).setOnClickListener {
            SRouterKit.startActivity(path = "activity/router2")
        }
        Log.i("SModuleServiceKit",SModuleServiceKit.getInstance().get<DefService>("defService")?.getName()?:"未获取到数据")
    }
}