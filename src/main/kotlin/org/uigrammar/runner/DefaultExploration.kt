package org.uigrammar.runner

import kotlinx.coroutines.runBlocking
import org.droidmate.api.ExplorationAPI
import org.uigrammar.exploration.CustomModelProvider

class DefaultExploration {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                explore(args)
            }
        }

        @JvmStatic
        suspend fun explore(args: Array<String>) {
            val cfg = ExplorationAPI.config(args)

            val modelProvider = CustomModelProvider()
            ExplorationAPI.explore(
                cfg,
                watcher = emptyList(),
                modelProvider = modelProvider
            )
        }
    }
}
