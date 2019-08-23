package org.uigrammar.runner

import org.droidmate.api.ExplorationAPI
import org.droidmate.command.ExploreCommandBuilder
import org.droidmate.configuration.ConfigProperties
import org.droidmate.configuration.ConfigurationWrapper
import org.droidmate.device.android_sdk.Apk
import org.uigrammar.exploration.CustomModelProvider
import org.droidmate.misc.FailableExploration
import java.util.UUID

object GrammarExploration {
    suspend fun exploreWithGrammarInput(
        cfg: ConfigurationWrapper,
        input: String,
        grammarMapping: Map<String, UUID>
    ): Map<Apk, FailableExploration> {

        val builder = ExploreCommandBuilder.fromConfig(cfg)
        builder
            .withStrategy(
                GrammarStrategy(
                    builder.getNextSelectorPriority(),
                    input,
                    grammarMapping,
                    cfg[ConfigProperties.Exploration.widgetActionDelay]
                )
            )

        val modelProvider = CustomModelProvider()
        return ExplorationAPI.explore(
            cfg,
            commandBuilder = builder,
            modelProvider = modelProvider,
            watcher = emptyList()
        )
    }
}