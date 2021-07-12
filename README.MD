## Router 路由基础库

- [X] 基础跳转完成 ：`SRouterKit`
- [X] 模块间数据交换获取： `SModuleServiceKit`
- [X] 输出相关信息的`json` ：`app/routerPluginJson`
- [X] 优化异常提示
- [X] 支持Scheme跳转
- [X] 返回路由跳转执行结果


插件部分实现逻辑：[router_plugin](https://github.com/SheTieJun/router_plugin)
> 大概思路：transform -> 扫描注解(SRouter和SModuleService) -> ASM(字节码插入到SRouterKit和SModuleServiceKit)->init初始化注册相关信息

### 集成说明
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


3. `app` 主模块

```groovy
apply plugin: 'com.android.application'
apply plugin: 'shetj.plugin.scanRouter'
```

4. 其他功能模块`module`
```groovy
if (releaseConfig.Fun2isLibrary) {
    //如果整个模块的功能要发布就当作一个lib
    apply plugin: 'com.android.library'
} else {
    //否则把模块当成APP
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
//初始化路由模块，注册相关路由
SRouterKit.init(application.applicationContext)
//初始化服务模块，注册相关服务
SModuleServiceKit.init()
```

```kotlin
//跳转
SRouterKit.startActivity(this,path,bundle = bundle,mapInfo = mapInfo,requestCode = requestCode)
```
```
       startRoute {
            path = "activity/router2xxx"
            mapInfo = null
            bundle = null
            requestCode = 1
        }
```
通过scheme跳转

```kotlin
<activity android:name=".Router2Activity">
     <intent-filter> <!--URL Scheme启动-->
          <!--必有项-->
          <action android:name="android.intent.action.VIEW"/>
          <!--如果希望该应用可以通过浏览器的连接启动，则添加该项-->
          <category android:name="android.intent.category.BROWSABLE"/>
          <!--表示该页面可以被隐式调用，必须加上该项-->
          <category android:name="android.intent.category.DEFAULT"/>
          <!--协议部分-->
          <data android:scheme="router"
              android:host="activity"
              android:path="/scheme/router2"/>
     </intent-filter>
</activity>
```
```kotlin
SRouterKit.startActivity("router://activity/scheme/router2?key=1&key2=2")
```

> 当scheme对应的activity不存在的时候，获取host+path,进行本地路由注册表查询
> haha://activity/scheme/router2,因为scheme对应的activity不存在的时候，所有会去查找activity/scheme/router2的activity

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