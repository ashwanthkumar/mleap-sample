package brand.intent.serve

object BrandIntent extends App {
  // by default bound to all interfaces
  val host        = sys.env.getOrElse("HOST", "0.0.0.0")
  val port        = sys.env.getOrElse("PORT", "5566").toInt
  val brandModelPath   = sys.env.getOrElse("BRAND_MODEL_PATH", "../models/v21/brand/model")
  val brandLabelPath   = sys.env.getOrElse("BRAND_LABEL_PATH", "../models/v21/brand/labels.csv")
  val accessoryModelPath   = sys.env.getOrElse("ACCESSORY_MODEL_PATH", "../models/v21/accessories/model")
  val accessoryLabelPath   = sys.env.getOrElse("ACCESSORY_LABEL_PATH", "../models/v21/accessories/labels.csv")

  val labelsPath  = sys.env.getOrElse("LABELS_PATH", "../models/labels.csv")
  val brandModel = Model(brandModelPath, brandLabelPath, "brands")
  val accessoryModel = Model(accessoryModelPath, accessoryLabelPath, "accessory")
  val service     = new ServiceRouter(brandModel, accessoryModel)
  service.bind(host, port)
}
