package brand.intent.serve

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.{Logging => AkkaLogging}
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.google.common.base.Stopwatch
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.language.postfixOps


trait RawResponse

trait CorsSupport {
  lazy val allowedOrigin = HttpOriginRange.*

  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`(allowedOrigin),
      `Access-Control-Allow-Headers`("Accept", "Content-Type", "X-Requested-With", "Access-Control-Allow-Origin")
    )
  }

  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, GET)))
  }

  def corsHandler(r: Route) = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }
}

case class ContentResponse(content: String) extends RawResponse

case class EntityResponse(datum: Any) extends RawResponse

class Service(brand: Model, accessory: Model)
             (implicit val materializer: ActorMaterializer,
              implicit val ec: ExecutionContext) {
  val logger = LoggerFactory.getLogger(classOf[Service])

  // case, cover and screen
  def predictBrands(query: String, n: Int) = {
    val watch = new Stopwatch()
    watch.start()

    val (predictions, isAccessory) = {
      if(accessoryModelPredicate(query)) accessory.predict(query) -> true
      else brand.predict(query) -> false
    }
    val topNPredictions = predictions.take(n)
    watch.stop()
    val elapsedTimeInMicros = watch.elapsedTime(TimeUnit.MICROSECONDS)
    logger.info("predictBrands2 took: " + elapsedTimeInMicros + " micros")
    Map("query" -> query, "isAccessory" -> isAccessory, "brands" -> topNPredictions)
  }

  lazy val accessoryDecider = Set("case", "cover", "screen")
  protected def accessoryModelPredicate(input: String) = {
    // TODO - Replace with actual tokenization we use today
    input.split(" ").map(StringUtils.trimToEmpty).distinct.exists(accessoryDecider)
  }

}

class ServiceRouter(brand: Model, accessory: Model) extends JacksonSupport with CorsSupport {

  implicit val system = ActorSystem("brand-intent")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val logger = AkkaLogging(system, getClass)
  val httpClient: HttpExt = Http(system)

  val service = new Service(brand, accessory)

  def health = path("v1" / "health") {
    get {
      complete(OK)
    }
  }

  def playground = pathPrefix("playground") {
    getFromResourceDirectory("swagger") ~ redirectToTrailingSlashIfMissing(MovedPermanently) {
      pathSingleSlash(getFromResource("swagger/index.html"))
    }
  }

  def predictBrand = path("v1" / "predict" / "brand") {
    get {
      parameter('q.as[String], 'n.as[Int] ?) { case (query, n) =>
        complete(ToResponseMarshallable(service.predictBrands(query, n.getOrElse(10))))
      }
    }
  }

  val routes = playground ~ health ~ predictBrand

  def bind(host: String, port: Int) = {
    logger.info(s"Starting server on $host:$port")
    Http().bindAndHandle(Route.handlerFlow(corsHandler(routes)), host, port, log = logger)
  }

}
