package coltfred

import org.scalatest._

abstract class BaseSpec
    extends FlatSpec
    with Matchers
    with OptionValues
    with TryValues
    with EitherValues
    with prop.PropertyChecks {
  //Allows for .toLeft and .toRight to extract with less pain
  implicit class ScalazEitherHelper[E, R](either: scalaz.\/[E, R]) {
    def toRight: R = {
      either.getOrElse(fail("You asked for Right, but it was Left."))
    }
    def toLeft: E = {
      either.swap.getOrElse(fail("You asked for Left, but it was Right."))
    }
  }
}
