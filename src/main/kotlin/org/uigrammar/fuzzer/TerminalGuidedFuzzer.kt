package org.uigrammar.fuzzer

import org.uigrammar.grammar.Grammar
import org.uigrammar.grammar.Production
import org.uigrammar.grammar.Symbol
import kotlin.random.Random

open class TerminalGuidedFuzzer(
    grammar: Grammar,
    random: Random = Random(0),
    printLog: Boolean = false
) : CoverageGuidedFuzzer(grammar, random, printLog) {

    override val nonCoveredSymbols
        get() = grammar.definedTerminals()
            .filterNot { it in coveredSymbols }
            .toSet()

    override fun Production.uncoveredSymbols(): Set<Symbol> {
        return this.terminals
            .filterNot { it in coveredSymbols }
            .toSet()
    }

    override fun onExpanded(node: Node, newNodes: List<Node>) {
        val terminals = newNodes
            .filter { it.isTerminal() }
            .map { it.value }

        coveredSymbols.addAll(terminals)
    }
}