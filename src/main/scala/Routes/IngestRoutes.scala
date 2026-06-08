package Routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.example.lumina.services.IngestService
import com.example.lumina.types.CreateSpanRequest
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

object IngestRoutes {

  def ingestRoutes[F[_]: Concurrent](ingestService: IngestService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case req @ POST -> Root / "ingest" / "spans" =>
        for {
          body <- req.as[CreateSpanRequest]
          _    <- ingestService.pushSpans(body.spans)
          resp <- Ok()
        } yield resp
    }
  }

}
