package Routes

import cats.effect.Concurrent
import com.example.lumina.services.TraceService

object TraceRoutes {
  def traceRoutes[F[_]: Concurrent](traceService: TraceService[F]) = ???
}
