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
            SRouterKit.startActivity(this@MainActivity, "activity/router")
        }
        findViewById<View>(R.id.startJump2).setOnClickListener {
            SRouterKit.startActivity(path = "activity/router2")
        }


    }
}