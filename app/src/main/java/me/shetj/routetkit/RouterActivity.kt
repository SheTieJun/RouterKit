package me.shetj.routetkit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.shetj.annotation.SRouter
import me.shetj.router.startActivity


@SRouter(path = "activity/router")
class RouterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)
        Text()

        //测试错误提示
        startActivity {
            path = "activity/router2xxx"
            intentInfo = null
            bundle = null
            requestCode = 1
        }
    }
}