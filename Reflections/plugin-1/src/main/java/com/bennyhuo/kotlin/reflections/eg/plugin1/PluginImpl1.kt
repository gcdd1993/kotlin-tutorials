package com.bennyhuo.kotlin.reflections.eg.plugin1

import com.bennyhuo.kotlin.reflections.eg.plugincommon.Plugin

class PluginImpl1 : Plugin {
    override fun start() {
        println("Plugin1: Start")
        newMethod()
    }

    fun newMethod() {
        println("newMethod called!! 1")
    }

    override fun stop() {
        println("Plugin1: Stop")
    }
}
