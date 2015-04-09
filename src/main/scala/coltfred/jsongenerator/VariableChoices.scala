package coltfred.jsongenerator

import scalaz.{ \/, Functor }
import scalaz.syntax.id._

/**
 * A class that guarentees that the variable choices
 * have a probability of 1.0. Use the companion object to construct one.
 */
case class VariableChoices[A] private (values: List[Variable[A]]) {
  //.get works because the probability adds up to 1 always.
  def findByProb(d: Double): Variable[A] = values.find(_.range.contains(d)).get
}
object VariableChoices {
  def apply[A](p: ParsedVariable[A]): Error \/ VariableChoices[A] = {
    apply(p, None)
  }

  def apply[A](p: ParsedVariable[A], parent: Option[Variable[A]]): Error \/ VariableChoices[A] = {
    sumProbabilities(p.label, p.choices, parent).map(VariableChoices(_))
  }

  //Figure out if the probability of the ParsedVariableChoice adds up to 1.0
  //and parse the result into a Variable
  private def sumProbabilities[A](label: String, l: List[ParsedVariableChoice[A]], parent: Option[Variable[A]] = None): Error \/ List[Variable[A]] = {
    val (finalProb, result) = l.foldLeft((BigDecimal(0.0), List[Variable[A]]())) {
      case ((prob, accList), next) =>
        val newProb = prob + next.probability
        (newProb, Variable(label, next.value, ProbabilityRange(prob, newProb), parent) :: accList)
    }

    if (finalProb != 1.0)
      ProbabilityError(s"The probability didn't add up to 1.0 for: '$label'").left
    else
      result.reverse.right
  }
}
