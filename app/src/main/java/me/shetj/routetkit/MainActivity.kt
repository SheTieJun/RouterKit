package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import me.shetj.annotation.SRouter
import me.shetj.router.SRouterKit
import java.util.HashSet

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.startJump).setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                SRouterKit.isDebug = true
                SRouterKit.getInstance().startJump(this@MainActivity,"activity/router")
            }
        })
    }
}