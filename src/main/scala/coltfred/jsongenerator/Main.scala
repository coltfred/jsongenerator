package coltfred.jsongenerator

import scalaz.effect.IO
import scalaz._, Scalaz._
import com.nicta.rng.Rng

object Main extends scalaz.effect.SafeApp {
  //implicit vals to allow closing of Source and the output stream.
  implicit val resource = new scalaz.effect.Resource[scala.io.BufferedSource] { def close(b: scala.io.BufferedSource): IO[Unit] = IO(b.close) }
  implicit val resource2 = new scalaz.effect.Resource[java.io.OutputStreamWriter] { def close(b: java.io.OutputStreamWriter): IO[Unit] = IO(b.close) }
  private[this] final val Utf8Encoding = "utf-8"
  private[this] val recordGenerator = new RngOutputRecordGenerator()
  override def runl(args: List[String]): IO[Unit] = {
    if (args.length != 3) {
      IO.putStrLn("Usage: Main <input json> <output dir> <# of records>")
    } else {
      val inputFilename = args(0)
      val outputFilename = args(1)
      val numRecords = \/.fromTryCatch(args(2).toInt).leftMap(ex => ParseError(ex.getMessage))

      val eitherT = for {
        recordCount <- EitherT(numRecords.point[IO])
        input <- readFile(inputFilename)
        //The values don't matter as long as they parse.
        //Could be changed if we want to be more strict.
        graph <- EitherT(Graph.fromJson[argonaut.Json](input).point[IO])
        randomRecords = recordGenerator.generateValues[argonaut.Json](graph).fill(recordCount).run
        outputs <- EitherT.right(randomRecords)
        _ <- writeFile(outputFilename, outputs)
      } yield ()

      eitherT.run.flatMap(_.fold(printErrorAndExit, _.point[IO]))
    }
  }

  def printErrorAndExit(error: Error): IO[Unit] = for {
    _ <- IO.putStrLn(error.toString)
    _ <- IO(sys.exit(1))
  } yield ()

  def readFile(filename: String): EitherT[IO, Error, String] = EitherT {
    import scala.io.BufferedSource
    IO(scala.io.Source.fromFile(filename, Utf8Encoding)).using { source: BufferedSource =>
      IO(source.getLines.mkString("\n"))
    }.catchLeft.map(_.leftMap(FileReadError(_)))
  }

  def writeFile[A: argonaut.EncodeJson](outputFilename: String, actions: List[OutputRecord[A]]): EitherT[IO, Error, Unit] = EitherT {
    import java.io._
    import argonaut._, Argonaut._
    def initFile(fileName: String, append: Boolean = false): OutputStreamWriter =
      new OutputStreamWriter(new FileOutputStream(fileName, append), Utf8Encoding)

    IO(initFile(outputFilename)).using { writer: OutputStreamWriter =>
      IO(writer.write(actions.asJson.spaces2))
    }.catchLeft.map(_.leftMap(FileWriteError(_)))
  }
}

