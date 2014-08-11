package com.lucidchart.open.relate

import java.sql.PreparedStatement
import scalaz.effect.IO

private[relate] object RowIterator {
  def apply[A](parser: RowParser[A], stmt: PreparedStatement, resultSet: IO[SqlResult]) = new RowIterator(parser, stmt, resultSet)
}

private[relate] class RowIterator[A](parser: RowParser[A], stmt: PreparedStatement, result: IO[SqlResult]) extends Iterator[A] {

  private var _hasNext: IO[Boolean] = for (res <- result) yield (res.next())

  /**
   * Make certain that all resources are closed
   */
  override def finalize() {
    close()
  }

  /**
   * Determine whether there is another row or not
   * @return whether there is another row
   */
  override def hasNext(): Boolean = _hasNext.unsafePerformIO()

  /**
   * Parse the next row using the RowParser passed into the class
   * @return the parsed record
   */
  override def next(): A = {
    (for {
      res <- result
      hasNext <- _hasNext
      ret <- parser(res)
    } yield {
      if (hasNext) {
        _hasNext = for (res <- result) yield (res.next())
      }

      if (!hasNext) {
        for (_ <- close()) yield ()
      }

      ret
    }).unsafePerformIO
  }

  /**
   * Close up resources
   */
  private def close(): IO[Unit] = {
    if (!stmt.isClosed()) {
      stmt.close()
    }

    for {
      res <- result
    } yield {
      if (!res.resultSet.isClosed())
        res.resultSet.close()
    }
  }

}
