package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.shetj.annotation.SRouter
import me.shetj.router.SRouterKit.Companion.addToRouter


class Router2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)
    }
}