package coltfred.jsongenerator

sealed trait Error { def message: String }
case class ParseError(message: String) extends Error
case class FileReadError(ex: Throwable) extends Error {
  def message: String = ex.getMessage
}
case class FileWriteError(ex: Throwable) extends Error {
  def message: String = ex.getMessage
}
case class ProbabilityError(message: String) extends Error
