package me.shetj.routetkit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.shetj.annotation.SRouter

@SRouter(path = "shell/activity/index")
class Router2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)
        println(intent.getStringExtra("key").toString())
        println(intent.getStringExtra("key2").toString())
    }
}