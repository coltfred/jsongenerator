package coltfred.jsongenerator

import scalaz.\/
import argonaut._, Argonaut._

case class ParsedVariable[A](label: String, choices: List[ParsedVariableChoice[A]])
case class ParsedVariableChoice[A](value: A, probability: Double, dependents: List[ParsedVariable[A]])

object ParsedVariableChoice {
  implicit def parsedChoiceCodec[A: DecodeJson]: DecodeJson[ParsedVariableChoice[A]] = DecodeJson[ParsedVariableChoice[A]](
    c => for {
      n <- (c --\ "value").as[A]
      p <- (c --\ "probability").as[Double]
      ds <- (c --\ "dependents").as[List[ParsedVariable[A]]](ListDecodeJson(ParsedVariable.parsedRecordCodec[A])) ||| DecodeResult.ok(List[ParsedVariable[A]]())
    } yield ParsedVariableChoice(n, p, ds)
  )
}

object ParsedVariable {
  implicit def parsedRecordCodec[A: DecodeJson]: DecodeJson[ParsedVariable[A]] =
    jdecode2L[String, List[ParsedVariableChoice[A]], ParsedVariable[A]](ParsedVariable.apply)("label", "choices")

  def parse[A: DecodeJson](s: String): Error \/ ParsedVariable[A] = s.decodeEither[ParsedVariable[A]].leftMap(ParseError(_))
}
