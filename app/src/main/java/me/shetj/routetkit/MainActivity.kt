package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import me.shetj.router.SRouterKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SRouterKit.isDebug = true
        SRouterKit.init(this)
        findViewById<View>(R.id.startJump).setOnClickListener {

            SRouterKit.getInstance().startJump(this@MainActivity, "activity/router")
        }
    }
}