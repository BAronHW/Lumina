package Routes

import cats.effect.Concurrent
import com.example.lumina.services.IngestService

object IngestRoutes {

  def ingestRoutes[F[_]: Concurrent](ingestService: IngestService[F]) = {}

}
