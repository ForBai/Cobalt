package org.cobalt.api.pathfinder.pathing.processing

import org.cobalt.api.pathfinder.pathing.processing.context.EvaluationContext
import org.cobalt.api.pathfinder.pathing.processing.context.SearchContext

object Validators {

  fun allOf(vararg validators: ValidationProcessor): ValidationProcessor {
    return AllOfValidator(*validators)
  }

  fun allOf(validators: List<ValidationProcessor>): ValidationProcessor {
    return AllOfValidator(validators)
  }

  fun anyOf(vararg validators: ValidationProcessor): ValidationProcessor {
    return AnyOfValidator(*validators)
  }

  fun anyOf(validators: List<ValidationProcessor>): ValidationProcessor {
    return AnyOfValidator(validators)
  }

  fun noneOf(vararg validators: ValidationProcessor): ValidationProcessor {
    return NoneOfValidator(*validators)
  }

  fun noneOf(validators: List<ValidationProcessor>): ValidationProcessor {
    return NoneOfValidator(validators)
  }

  fun not(validator: ValidationProcessor): ValidationProcessor {
    return NotValidator(validator)
  }

  fun alwaysTrue(): ValidationProcessor {
    return AlwaysTrueValidator
  }

  fun alwaysFalse(): ValidationProcessor {
    return AlwaysFalseValidator
  }

  private fun copyAndFilterNulls(vararg validators: ValidationProcessor?): List<ValidationProcessor> {
    return validators.filterNotNull()
  }

  private fun copyAndFilterNulls(validators: List<ValidationProcessor?>?): List<ValidationProcessor> {
    return validators?.filterNotNull() ?: emptyList()
  }

  private abstract class AbstractCompositeValidator : ValidationProcessor {
    protected val children: List<ValidationProcessor>

    constructor(vararg validators: ValidationProcessor?) {
      this.children = copyAndFilterNulls(*validators)
    }

    constructor(validators: List<ValidationProcessor?>?) {
      this.children = copyAndFilterNulls(validators)
    }

    override fun initializeSearch(context: SearchContext) {
      children.forEach { it.initializeSearch(context) }
    }

    override fun finalizeSearch(context: SearchContext) {
      children.forEach { it.finalizeSearch(context) }
    }
  }

  private class AllOfValidator : AbstractCompositeValidator {
    constructor(vararg validators: ValidationProcessor?) : super(*validators)
    constructor(validators: List<ValidationProcessor?>?) : super(validators)

    override fun isValid(context: EvaluationContext): Boolean {
      return children.all { it.isValid(context) }
    }
  }

  private class AnyOfValidator : AbstractCompositeValidator {
    constructor(vararg validators: ValidationProcessor?) : super(*validators)
    constructor(validators: List<ValidationProcessor?>?) : super(validators)

    override fun isValid(context: EvaluationContext): Boolean {
      if (children.isEmpty()) return false
      return children.any { it.isValid(context) }
    }
  }

  private class NoneOfValidator : AbstractCompositeValidator {
    constructor(vararg validators: ValidationProcessor?) : super(*validators)
    constructor(validators: List<ValidationProcessor?>?) : super(validators)

    override fun isValid(context: EvaluationContext): Boolean {
      return children.none { it.isValid(context) }
    }
  }

  private class NotValidator(private val child: ValidationProcessor) : ValidationProcessor {
    override fun initializeSearch(context: SearchContext) {
      child.initializeSearch(context)
    }

    override fun isValid(context: EvaluationContext): Boolean {
      return !child.isValid(context)
    }

    override fun finalizeSearch(context: SearchContext) {
      child.finalizeSearch(context)
    }
  }

  private object AlwaysTrueValidator : ValidationProcessor {
    override fun isValid(context: EvaluationContext): Boolean = true
  }

  private object AlwaysFalseValidator : ValidationProcessor {
    override fun isValid(context: EvaluationContext): Boolean = false
  }
}
