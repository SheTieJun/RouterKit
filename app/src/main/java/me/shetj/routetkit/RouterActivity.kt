package me.shetj.routetkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.shetj.annotation.SRouter
import me.shetj.router.SRouterKit


@SRouter(path = "activity/router")
class RouterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)
        Text()
        SRouterKit.startActivity(path = "activity/router2xxx")
    }
}