package coltfred.jsongenerator

import scalaz._, Scalaz._
import com.nicta.rng.Rng
//Produce output records based on the value of doubleM
//This is available as a trait for testing.
trait OutputRecordGenerator[M[+_]] {
  implicit def M: Monad[M]
  protected def doubleM: M[Double]
  def generateValues[A](g: Graph[A]): M[OutputRecord[A]] = {
    def produceOutput(l: NonEmptyList[Variable[A]]): OutputRecord[A] = {
      OutputRecord(l.list.map(r => r.label -> r.value))
    }

    //random entries in the adjacency list weighted for probability
    val rng: M[Variable[A] ==>> List[Variable[A]]] = g.adjacencyList.map { l =>
      l.map { x => doubleM.map { d => x.findByProb(d) } }.sequenceU
    }.sequenceU

    def generateValuesR(acc: NonEmptyList[List[Variable[A]]]): M[NonEmptyList[Variable[A]]] = {
      acc.head.flatMap(g.adjacencyList.lookup(_).toList.flatten) match {
        // Hit end of graph, collapse inner lists and put the result in M
        case Nil =>
          val list = acc.list.flatten
          NonEmptyList.nel(list.head, list.tail).point[M]
        case _ => rng.flatMap { m =>
          //Get a random values for the next moves and recurse on
          val listForFront = acc.head.map(m.lookup(_).toList.flatten).flatten
          generateValuesR(listForFront <:: acc)
        }
      }
    }
    //Select a root based on doubleM then search the graph recursively
    doubleM.flatMap { d =>
      generateValuesR(g.rootVariable.findByProb(d).point[List].point[NonEmptyList])
    }.map(produceOutput)
  }
}

class RngOutputRecordGenerator(implicit val M: Monad[Rng]) extends OutputRecordGenerator[Rng] {
  protected def doubleM: Rng[Double] = Rng.double
}
