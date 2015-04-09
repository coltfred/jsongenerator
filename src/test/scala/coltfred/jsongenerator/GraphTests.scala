package coltfred.jsongenerator

import scalaz.==>>
import scalaz._, Scalaz._
class GraphTests extends coltfred.BaseSpec {
  val nonEmptyVariableChoices = VariableChoices(ParsedVariable("label", List(ParsedVariableChoice(1, 0.01, Nil), ParsedVariableChoice(2, 0.99, Nil)))).toRight
  val variable = Variable[Int]("label", 1, ProbabilityRange.empty, None)

  behavior of "Graph.choicesToAdjacencyList"

  it should "create empty for Nil" in {
    Graph.choicesToAdjacencyList(variable, Nil) shouldBe ==>>.empty
  }

  it should "create nonempty for non Nil" in {
    Graph.choicesToAdjacencyList[Int](variable, List(nonEmptyVariableChoices)) should not be ('isEmpty)
  }

  behavior of "Graph.fromJson"

  it should "fail for bad json" in {
    Graph.fromJson[String]("") shouldBe 'isLeft
  }

  it should "fail for probability that doesn't add up to 1.0" in {
    val result = Graph.fromJson[String]("""{"label":"hair color","choices":[{"value":"blonde","probability":0.3,"dependents":[]}]}""")
    val error = result.toLeft
    error.isInstanceOf[ProbabilityError] shouldBe true
    error.message should include("hair color")
  }

  it should "succeed for good json root only" in {
    val result = Graph.fromJson[String]("""{"label":"hair color","choices":[{"value":"blonde","probability":1.0,"dependents":[]}]}""")
    val graph = result.toRight
    graph.rootVariable.values should have length (1)
    graph.adjacencyList shouldBe 'empty
  }

  it should "succeed for nested variables that have the same name" in {
    val json = """{"label":"label1","choices":[{"value":1,"probability":1.0,"dependents":[{"label":"label1","choices":[{"value":1,"probability":1.0,"dependents":[]}]}]}]}"""
    val graph = Graph.fromJson[Int](json).toRight
    val rootVariable = Variable("label1", 1, ProbabilityRange(0.0, 1.0), None)
    graph.rootVariable.values should have length (1)
    graph.rootVariable.values.head shouldBe rootVariable
    graph.adjacencyList.toList should have length (1)
    val adjacencyValue = graph.adjacencyList.toList.map(t => t._1 -> t._2.flatMap(_.values)).head
    adjacencyValue shouldBe rootVariable -> Variable("label1", 1, ProbabilityRange(0.0, 1.0), rootVariable.some).point[List]
  }
}
