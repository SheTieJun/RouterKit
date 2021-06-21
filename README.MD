## Router 路由基础库

- [X] 基础跳转完成 ：`SRouterKit`
- [X] 模块间数据交换获取： `SModuleServiceKit`


### 集成方法

1. `build.gradle`
```groovy
maven { url 'https://jitpack.io' }
```

[![](https://jitpack.io/v/SheTieJun/router_plugin.svg)](https://jitpack.io/#SheTieJun/router_plugin)
```groovy
classpath 'com.github.SheTieJun:router_plugin:0.0.1'
```

2. `router`组件module
[![](https://jitpack.io/v/SheTieJun/RouterKit.svg)](https://jitpack.io/#SheTieJun/RouterKit)
```groovy
    api 'com.github.SheTieJun.RouterKit:router-annotation:0.0.2'
    api 'com.github.SheTieJun.RouterKit:router-kit:0.0.2'
``` 


3. `app` 模块

```groovy
apply plugin: 'shetj.plugin.scanRouter'
```

4. 其他功能模块`module`
```groovy
if (releaseConfig.fun1Release) {
    apply plugin: 'com.android.library'
} else {
    apply plugin: 'com.android.application'
    apply plugin: 'shetj.plugin.scanRouter'
}
```

5. 需要路由的activity添加注解
```kotlin
@SRouter(path = "/app/main")
```

6. 初始化和跳转

```kotlin
 SRouterKit.isDebug = isDebug
 SRouterKit.init(application.applicationContext)
```

```kotlin
SRouterKit.startActivity(this,path,bundle = bundle,mapInfo = mapInfo,requestCode = requestCode)
```

7. 模块化服务
```
@SModuleService("defService")
class DefService:IModuleService {

    fun getName() = "defService"

}
```
获取并且使用
```
SModuleServiceKit.getInstance().get<DefService>("defService")?.getName()
```