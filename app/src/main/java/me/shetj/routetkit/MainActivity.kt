package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import me.shetj.router.SRouterKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.startJump1).setOnClickListener {
            SRouterKit.getInstance().startJump(this@MainActivity, "activity/router")
//            SRouterKit.getInstance().startJump("activity/router2")
        }
        findViewById<View>(R.id.startJump2).setOnClickListener {
            SRouterKit.getInstance().startJump(path = "activity/router2")
        }


    }
}