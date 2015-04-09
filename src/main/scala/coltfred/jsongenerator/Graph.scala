package coltfred.jsongenerator

import scalaz._, Scalaz._
import argonaut.DecodeJson

/**
 * A graph is a root variable which is some number of Variables that could be
 * chosen as the starting point of traversal. This is separate from adjacencyList
 * because the outside caller needs to know the possible starting places.
 */
trait Graph[A] {
  def rootVariable: VariableChoices[A]
  def adjacencyList: AdjacencyList[A]
}

object Graph {
  def apply[A](r: VariableChoices[A], adj: AdjacencyList[A]): Graph[A] = new Graph[A] {
    val rootVariable = r
    val adjacencyList = adj
  }

  def fromJson[A: DecodeJson](data: String): Error \/ Graph[A] = {
    for {
      parsedVariable <- ParsedVariable.parse[A](data)
      graph <- fromRootParsedVariable(parsedVariable)
    } yield graph
  }

  private[jsongenerator] def fromRootParsedVariable[A](p: ParsedVariable[A]): Error \/ Graph[A] = {
    for {
      root <- VariableChoices(p)
      adjList <- fromParsedVariables(expandChoices(root, p.choices))
    } yield apply(root, adjList)
  }

  //Return the extracted Variables with their children
  private def expandChoices[A](choice: VariableChoices[A], p: List[ParsedVariableChoice[A]]): List[(Variable[A], List[ParsedVariable[A]])] = {
    choice.values.zip(p.map(_.dependents))
  }

  private def combine[A](adjs: AdjacencyList[A]*) = {
    adjs.reduceOption(_ union _).getOrElse(==>>.empty)
  }
  /**
   * Given variables and the parsed variables they lead to, create an adjacency list
   */
  private def fromParsedVariables[A](l: List[(Variable[A], List[ParsedVariable[A]])]): Error \/ AdjacencyList[A] = {
    l.traverseU {
      case (variable, parsedVariables) => fromParsedVariable(variable, parsedVariables)
    }.map(combine)
  }

  /**
   * For a single variable create an node in the graph and call fromParsedVariables on all sub paths
   * from that node.
   */
  private[jsongenerator] def fromParsedVariable[A](r: Variable[A], p: List[ParsedVariable[A]]): Error \/ AdjacencyList[A] = {
    for {
      nextVariableChoices <- p.traverseU(VariableChoices(_, r.some))
      nextLevel = nextVariableChoices.zip(p.map(_.choices)).flatMap(t => expandChoices(t._1, t._2))
      recursiveAdjList <- fromParsedVariables(nextLevel)
      adjacencyList = choicesToAdjacencyList(r, nextVariableChoices)
    } yield combine(recursiveAdjList, adjacencyList)
  }

  private[jsongenerator] def choicesToAdjacencyList[A](r: Variable[A], l: List[VariableChoices[A]]): AdjacencyList[A] = {
    l match {
      case Nil => ==>>.empty
      case ll => ==>>(r -> ll)
    }
  }
}
