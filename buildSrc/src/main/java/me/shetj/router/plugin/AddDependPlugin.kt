package me.shetj.router.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project


class AddDependPlugin:Plugin<Project> {

    private val  version = "0.0.8"

    override fun apply(project: Project) {
        project.afterEvaluate {
            project.dependencies.apply {
                val type = project.gradle.gradleVersion.let {
                    if (  it.split(".")[0].toInt() >=3) {
                        "api"
                    } else {
                        "compile"
                    }
                }
                add(type, "com.github.SheTieJun.RouterKit:router-annotation:$version")
                add(type, "com.github.SheTieJun.RouterKit:router-kit:$version")
            }
        }
    }
}