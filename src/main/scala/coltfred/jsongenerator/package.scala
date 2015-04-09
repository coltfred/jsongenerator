package coltfred

import scalaz.==>>
package object jsongenerator {
  //Some variable to N different Dependent choices
  type AdjacencyList[A] = Variable[A] ==>> List[VariableChoices[A]]
}
