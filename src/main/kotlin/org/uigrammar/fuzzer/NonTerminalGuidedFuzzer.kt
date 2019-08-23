package org.uigrammar.fuzzer

import org.uigrammar.grammar.Grammar
import org.uigrammar.grammar.Production
import org.uigrammar.grammar.Symbol
import kotlin.random.Random

open class NonTerminalGuidedFuzzer(
    grammar: Grammar,
    random: Random = Random(0),
    printLog: Boolean = false
) : CoverageGuidedFuzzer(grammar, random, printLog) {
    override val nonCoveredSymbols
        get() = grammar.definedNonTerminals()
            .filterNot { it in coveredSymbols }
            .toSet()

    override fun onExpanded(node: Node, newNodes: List<Node>) {
        val nonTerminals = newNodes
            .filter { it.isNonTerminal() }
            .map { it.value }

        coveredSymbols.addAll(nonTerminals)
    }

    override fun Production.uncoveredSymbols(): Set<Symbol> {
        return this.nonTerminals
            .filterNot { it in coveredSymbols }
            .toSet()
    }
}