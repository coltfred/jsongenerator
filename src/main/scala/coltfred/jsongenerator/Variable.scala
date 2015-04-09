package coltfred.jsongenerator

import scalaz._, Scalaz._

case class Variable[A](label: String, value: A, range: ProbabilityRange, parent: Option[Variable[A]])
object Variable {
  implicit def order[A](implicit o: Order[Int]) = new scalaz.Order[Variable[A]] {
    //For my purpose order doesn't matter, but it's required for the ==>>. 
    //In retrospect a map that's unordered would have been better.
    def order(x: Variable[A], y: Variable[A]): Ordering = {
      o.order(x.hashCode, y.hashCode)
    }
    override def equal(r1: Variable[A], r2: Variable[A]) = r1 == r2
    override val equalIsNatural = true
  }

}

//Bottom inclusive top exclusive
case class ProbabilityRange(d1: BigDecimal, d2: BigDecimal) {
  def contains(d: BigDecimal): Boolean = d >= d1 && d < d2
}

object ProbabilityRange {
  val empty = ProbabilityRange(0.0, 0.0)
}

case class OutputRecord[A](outputs: List[(String, A)])
object OutputRecord {
  import argonaut._, Argonaut._
  implicit def codec[A](implicit encode: EncodeJson[A]) = EncodeJson[OutputRecord[A]] { outputRecord =>
    Json(outputRecord.outputs.map(t => t._1 -> t._2.asJson): _*)
  }
}
