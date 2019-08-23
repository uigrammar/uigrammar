package org.uigrammar.fuzzer

import org.uigrammar.grammar.Grammar
import org.uigrammar.grammar.Production
import org.uigrammar.grammar.Symbol
import org.uigrammar.mining.GrammarExtractor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random

class Fuzzer(
    private val grammar: Grammar,
    private val outputDir: Path,
    private val numSeeds: Int = 11,
    private val useCoverage: Boolean = false,
    private val random: Random = Random(0),
    private val debug: Boolean = false
) {
    constructor(grammar: Grammar, args: Array<String>) : this(
        grammar,
        Paths.get(args.getOrNull(1) ?: throw IOException("Missing output dir path")).toAbsolutePath(),
        args.getOrNull(2)?.toInt() ?: 11,
        args.getOrNull(3)?.toBoolean() ?: false
    )

    private fun Int.padStart(length: Int = 2): String {
        return this.toString().padStart(length, '0')
    }

    private fun fuzz(
        seed: Int,
        fuzzerProducer: (Map<Production, Set<Production>>, Int) -> CoverageGuidedFuzzer
    ): List<List<Symbol>> {
        val grammarMap = if (useCoverage) {
            grammar.asMap().toCoverageGrammar()
        } else {
            grammar.asMap()
        }

        val generator = fuzzerProducer(grammarMap, seed)
        val result = mutableListOf<List<Symbol>>()

        var count = 0
        while (generator.nonCoveredSymbols.isNotEmpty()) {

            val nonCoveredSymbolsBeforeRun = generator.nonCoveredSymbols
            val input = generator.fuzz()

            val newlyCovered = nonCoveredSymbolsBeforeRun - generator.nonCoveredSymbols

            if (debug) {
                println("Covered: $newlyCovered")
                println("Missing: ${generator.nonCoveredSymbols}")
            }

            if (generator.nonCoveredSymbols.isNotEmpty() && newlyCovered.isEmpty()) {
                log.error(
                    "No new terminals were covered in this run. " +
                            "Original: $nonCoveredSymbolsBeforeRun. Actual: ${generator.nonCoveredSymbols}"
                )

                count++

                if (count >= 2) {
                    break
                }
            } else {
                count = 0
            }

            result.add(input)
        }

        if (generator.nonCoveredSymbols.isNotEmpty()) {
            log.error(
                "Could not cover ${generator.nonCoveredSymbols.size} symbol (${
                generator.nonCoveredSymbols.size / grammar.definedTerminals().size.toLong()}%)"
            )
        }

        return result
    }

    private fun generateSeed(
        seed: Int,
        outputFile: Path,
        appendFile: Boolean,
        fuzzerProducer: (Map<Production, Set<Production>>, Int) -> CoverageGuidedFuzzer
    ) {
        val inputs = fuzz(seed, fuzzerProducer)

        val sb = StringBuilder()

        inputs.forEach { input ->
            sb.appendln(input
                .filter { it.value.contains("(") || it == Symbol.start }
                .joinToString(" ") { it.value })
        }

        if (appendFile && Files.exists(outputFile)) {
            Files.write(outputFile, sb.toString().toByteArray(), StandardOpenOption.APPEND)
        } else {
            Files.write(outputFile, sb.toString().toByteArray())
        }
    }

    fun fuzzAllSeeds() {
        for (seed in 0..numSeeds) {
            val fileName = if (useCoverage) {
                "coverageInputs"
            } else {
                "inputs"
            } + "${seed.padStart()}.txt"

            val outputFile = outputDir.resolve(fileName)
            generateSeed(seed, outputFile, false) { grammarMap, randomSeed ->
                CodeTerminalGuidedFuzzer(
                    Grammar(initialGrammar = grammarMap),
                    random = Random(randomSeed),
                    printLog = false
                )
            }
        }
    }

    /**
     *
     *
     * Computes sample size based on confidence of 85%, 90%, 95%, 97% or 99%
     * These confidence values correspond to these Z scores: 1.44, 1.65, 1.96, 2.17, 2.58
     *
     * E.g., sampleSize(650000,.95,.03) -> sample size of 1,066
     *
     * @param population - overall population size
     * @param confidence - confidence - 85%, 90%, 95%, 97% or 99% - uses 95%  if anything else is sent.
     * @param error - Margin of error.
     * @return - the number of samples.
     */
    private fun sampleSize(population: Int, confidence: Double, error: Double): Long {
        val z = when (confidence) {
            .85 -> 1.44
            .90 -> 1.65
            .95 -> 1.96
            .97 -> 2.17
            .99 -> 2.58
            else -> 1.96
        }
        val n = 0.25 * (z / error).pow(2.0)
        val size = ceil(population * n / (n + population - 1))
        return (size + .5).toLong()
    }

    private fun getTargetLOC(confidence: Double, error: Double): List<Symbol> {
        val coverageGrammar = Grammar(initialGrammar = grammar.asMap().toCoverageGrammar())
        val terminals = coverageGrammar.definedTerminals()
        val populationSize = terminals.size
        val sampleSize = sampleSize(populationSize, confidence, error)

        return (0..sampleSize).map {
            terminals.random(random)
        }
    }

    fun fuzzRandomSymbolsGrammar() {
        val symbols = getTargetLOC(0.9, 0.1)

        val symbolsGrammar = symbols.map { symbol ->
            Pair(symbol, grammar.asMap().toCoverageGrammar(symbol))
        }.toMap()

        Files.write(outputDir.resolve("targetSymbols.txt"),
            symbols.joinToString("\n") { it.toString() }.toByteArray())

        for (seed in 0..numSeeds) {
            val outputFile = outputDir
                .resolve("symbolInputs${seed.padStart()}.txt")
            Files.deleteIfExists(outputFile)

            log.info("Generating input set $seed")
            symbols.forEach { symbol ->
                val symbolGrammar = symbolsGrammar.getValue(symbol)
                generateSeed(seed, outputFile, true) { _, _ ->
                    SymbolGuidedFuzzer(
                        Grammar(initialGrammar = symbolGrammar),
                        random = Random(seed),
                        printLog = false,
                        symbol = symbol
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

        @JvmStatic
        fun main(args: Array<String>) {
            val grammar = GrammarExtractor.extract(args, true)

            val fuzzer = Fuzzer(grammar, args)
            // fuzzer.fuzzAllSeeds()
            fuzzer.fuzzRandomSymbolsGrammar()
        }
    }
}