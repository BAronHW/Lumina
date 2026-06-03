package Domain

import cats.data.OptionT
import java.util.UUID

trait ClientRepositoryAlgebra[F[_]] {
  def create(client: Client): F[Client]

  def update(client: Client): OptionT[F, Client]

  def get(clientId: UUID): OptionT[F, Client]

  def delete(clientId: UUID): OptionT[F, Client]

  def getByName(name: String): OptionT[F, Client]

  def deleteByName(name: String): OptionT[F, Client]

  def list(pageSize: Int, offset: Int): F[List[Client]]
}
