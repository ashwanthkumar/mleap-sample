package brand.intent.serve

import java.io.File
import java.util.concurrent.TimeUnit

import com.google.common.base.Stopwatch
import ml.combust.bundle.BundleFile
import ml.combust.mleap.runtime.MleapSupport._
import ml.combust.mleap.runtime._
import org.slf4j.LoggerFactory
import resource.managed

import scala.io.Source

case class Model(path: File, labels: File, name: String) {
  val logger = LoggerFactory.getLogger(classOf[Model])

  val underlyingModel = {
    val watch = new Stopwatch().start()
    logger.info("Attempting to load the " + name + " MLeap model")
    val model = (for (bf <- managed(BundleFile(path))) yield {
      bf.loadMleapBundle()
    }).tried.flatMap(identity).get.root
    watch.stop()
    logger.info("MLeap model loaded into memory. Took " + watch.elapsedTime(TimeUnit.MICROSECONDS) + " micros")
    model
  }

  val labelMapping: Map[Int, String] = Source.fromFile(labels)
    .getLines()
    .drop(1) // drop the headers
    .map { line =>
    val parts = line.split(",")
    parts(1).toInt -> parts(0)
  }.toMap

  def labelFor(index: Int) = labelMapping.apply(index)

  def predict(query: String): Iterable[Map[String, Any]] = {
    val dataset: LocalDataset = LocalDataset(Row(query))
    val leapFrame: DefaultLeapFrame = LeapFrame(underlyingModel.inputSchema, dataset)
    underlyingModel.transform(leapFrame).get
      .dataset
      .flatMap(row => row.getTensor[Double](4)
        .rawValues
        .zipWithIndex
        .sortBy(_._1)(Ordering[Double].reverse)
        .map { case (probability, idx) => Map("probability" -> probability, "name" -> labelMapping.apply(idx)) }
      )
  }
}

object Model {
  def apply(path: String, labels: String, name: String) = {
    new Model(
      path = new File(path).getAbsoluteFile,
      labels = new File(labels).getAbsoluteFile,
      name = name
    )
  }
}