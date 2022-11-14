package com.san68bot.alphaLib.control.logicControllers.rule

import java.lang.IllegalStateException

data class Rule(
    /**
     * The message to be displayed when the rule is violated
     */
    val message: String,

    /**
     * Condition that causes the rule to be violated
     */
    val cause: () -> Boolean
)

object RuleBook {
    /**
     * List of rules to be checked
     */
    private val ruleBook = mutableListOf<Rule>()

    /**
     * Add a rule to the rule book
     */
    fun add(vararg rules: Rule) {
        rules.forEach { rule ->
            ruleBook.add(rule)
        }
    }

    /**
     * Evaluate all rules in the rule book
     */
    fun evaluate() {
        ruleBook.forEach { rule ->
            if (rule.cause())
                throw IllegalStateException(rule.message)
        }
    }
}