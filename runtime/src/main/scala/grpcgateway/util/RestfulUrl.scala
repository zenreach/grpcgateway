package grpcgateway.util

import java.util

import grpcgateway.util.RestfulUrl._
import io.grpc.Status

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Try}

/** A container of extracted URI properties */
trait RestfulUrl {
  /**
    * A uniform way to access URL parameters extracted from named slots (e.g. "/{slot}/") and ordinary parameters (e.g. "?k=v")
    * @return named URL parameter extracted from a query uri with a UrlTemplate */
  def parameter(name: String): String
}

private final class PlainRestfulUrl(parameters: PathParams) extends RestfulUrl {
  override def parameter(name: String): String =
    Try(parameters.getOrDefault(name, new util.ArrayList[String]()).asScala.head)
      .recoverWith({
        case ex : NoSuchElementException => Failure(Status.INVALID_ARGUMENT.withDescription("invalid params")asRuntimeException())
        case ex : Throwable => Failure(ex)
      }).get
}

private final class MergedRestfulUrl(templateParams: TemplateParams, pathParams: PathParams) extends RestfulUrl {
  override def parameter(name: String): String = {
    if (templateParams.contains(name)) {
      templateParams(name)
    } else if (pathParams.containsKey(name)) {
      pathParams.get(name).asScala.head
    } else {
      null //throw new IllegalArgumentException(s"Property not found: $name")
    }
  }
}

private object RestfulUrl {
  /** parameters extracted from named slots such as "/{slot}/" */
  type PathParams = util.Map[String, util.List[String]]

  /** ordinary parameters such as "?k=v" */
  type TemplateParams = mutable.Map[String, String]
}
