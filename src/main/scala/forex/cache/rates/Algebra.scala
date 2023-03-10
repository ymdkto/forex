package forex.cache.rates

import errors._

trait Algebra[F[_], K, V] {
  def get(key: K): F[Error Either V]
}
