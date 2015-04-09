package coltfred.jsongenerator

class VariableChoicesTest extends coltfred.BaseSpec {
  behavior of "apply"

  it should "return left if probability isn't 1.0" in {
    val data = ParsedVariable("label", List(ParsedVariableChoice("value", 0.01, Nil), ParsedVariableChoice("value", 0.98, Nil)))
    VariableChoices(data).toLeft.message should include("probability")
  }

  it should "return non empty right if the probability is 1.0" in {
    val data = ParsedVariable("label", List(ParsedVariableChoice("value", 0.01, Nil), ParsedVariableChoice("value", 0.99, Nil)))
    VariableChoices(data).toRight.values should not be (Nil)
  }
}

