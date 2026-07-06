package Routes

import cats.effect.Concurrent
import com.example.lumina.services.SpanService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import cats.syntax.all.*
import Domain.Span.given

object SpanRoutes {
  def spanRoutes[F[_]: Concurrent](spanService: SpanService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] { case GET -> Root / "spans" / UUIDVar(spanId) =>
      spanService.getSpanById(spanId).flatMap {
        case Some(span) => Ok(span)
        case None       => NotFound()
      }
    }
  }

}
