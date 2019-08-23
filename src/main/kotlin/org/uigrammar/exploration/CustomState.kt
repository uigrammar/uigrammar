package org.uigrammar.exploration

import org.droidmate.explorationModel.interaction.State
import org.droidmate.explorationModel.interaction.Widget

class CustomState(_widgets: Collection<CustomWidget>, isHomeScreen: Boolean) : State<CustomWidget>(_widgets, isHomeScreen) {
    override fun isRelevantForId(w: Widget): Boolean {
        return super.isRelevantForId(w) && !(w as CustomWidget).isToast()
    }

    override val actionableWidgets: List<CustomWidget>
        get() = super.actionableWidgets
            .filterNot { it.isToast() }
            // .filter { it.hasValueForId() }

    override val visibleTargets: List<CustomWidget>
        get() = super.visibleTargets
            .filterNot { it.isToast() }
            // .filter { it.hasValueForId() }
}