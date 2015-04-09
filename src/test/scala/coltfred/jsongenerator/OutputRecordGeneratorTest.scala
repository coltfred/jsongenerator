package coltfred.jsongenerator

import scalaz._, Scalaz._
class OutputRecordGeneratorTest extends coltfred.BaseSpec {
  behavior of "generateValues"

  class IdOutputRecordGenerator(double: Double)(implicit val M: Monad[Id]) extends OutputRecordGenerator[Id] {
    def doubleM: Id[Double] = double
  }

  it should "produce expected results for simple graph" in {
    val parsedVariables = ParsedVariable("label", List(ParsedVariableChoice(1, 0.25, Nil), ParsedVariableChoice(2, 0.75, Nil)))
    val root = VariableChoices(parsedVariables).toRight
    val graph = Graph[Int](root, ==>>.empty)
    val smallGenerator = new IdOutputRecordGenerator(.2)
    smallGenerator.generateValues(graph) shouldBe OutputRecord[Int](List("label" -> 1))
  }

  it should "produce expected results for complex graph" in {
    val eyeColor = VariableChoices(ParsedVariable("ec", List(ParsedVariableChoice(1, 0.1, Nil), ParsedVariableChoice(2, 0.9, Nil)))).toRight
    val hairColor = VariableChoices(ParsedVariable("hc", List(ParsedVariableChoice(1, 0.50, Nil), ParsedVariableChoice(2, 0.2, Nil), ParsedVariableChoice(3, 0.3, Nil)))).toRight
    val height = VariableChoices(ParsedVariable("height", List(ParsedVariableChoice(1, 0.15, Nil), ParsedVariableChoice(2, 0.85, Nil)))).toRight
    val root = VariableChoices(ParsedVariable("root", List(ParsedVariableChoice(1, 0.25, Nil), ParsedVariableChoice(2, 0.75, Nil)))).toRight
    //1 in root -> List(eyeColor, height), 1 in eye Color -> hair color
    val graph = Graph[Int](root, ==>>.empty + (root.values.head -> List(eyeColor, height)) + (eyeColor.values.head -> hairColor.point[List]))
    //.2 from root should get root 1, ec 2 with no hc, height 2
    new IdOutputRecordGenerator(.2).generateValues(graph) shouldBe OutputRecord[Int](List("ec" -> 2, "height" -> 2, "root" -> 1))
    //.01 from root should get root 1, ec 1 and hc 1, height 1
    new IdOutputRecordGenerator(.01).generateValues(graph) shouldBe OutputRecord[Int](List("hc" -> 1, "ec" -> 1, "height" -> 1, "root" -> 1))
  }
}
