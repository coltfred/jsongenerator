package coltfred.jsongenerator

class ParsedVariableTest extends coltfred.BaseSpec {
  behavior of "ParsedVariable.parse"

  it should "fail for invalid json" in {
    ParsedVariable.parse[String]("").toLeft.message should include("JSON")
  }

  it should "parse for int values" in {
    val result = ParsedVariable.parse[Int]("""{"label":"label1","choices":[{"value":1,"probability":0.3,"dependents":[]}]}""").toRight
    result shouldBe ParsedVariable("label1", List(ParsedVariableChoice(1, 0.3, Nil)))
  }
}
