## Router 路由基础库

- [X] 基础跳转完成 ：`SRouterKit`
- [X] 模块间数据交换获取： `SModuleServiceKit`
- [X] 输出相关信息的`json` ：`app/routerPluginJson`


### 集成方法
集成插件
1. `build.gradle`
```groovy
maven { url 'https://jitpack.io' }
```

[![](https://jitpack.io/v/SheTieJun/router_plugin.svg)](https://jitpack.io/#SheTieJun/router_plugin)

```groovy
classpath 'com.github.SheTieJun:router_plugin:+'
```

2. `router`组件module

[![](https://jitpack.io/v/SheTieJun/RouterKit.svg)](https://jitpack.io/#SheTieJun/RouterKit)

```groovy
api 'com.github.SheTieJun.RouterKit:router-annotation:+'
api 'com.github.SheTieJun.RouterKit:router-kit:+'
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
//初始化路由模块 
SRouterKit.isDebug = isDebug
 SRouterKit.init(application.applicationContext)
//初始化服务模块 
SModuleServiceKit.init()
```

```kotlin
//跳转
SRouterKit.startActivity(this,path,bundle = bundle,mapInfo = mapInfo,requestCode = requestCode)
```

7. ModuleService服务

  > 必须实现`IModuleService`，用来过滤混淆，插件扫描判断是`ModuleService`
```
@SModuleService("defService")
class DefServiceImp:UserService,IModuleService
```
获取并且使用
```
SModuleServiceKit.getInstance().get<UserService>("defService")?.getName()
```