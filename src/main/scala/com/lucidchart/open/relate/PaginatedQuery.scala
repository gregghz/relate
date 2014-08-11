package com.lucidchart.open.relate

import java.sql.Connection
import scala.collection.mutable.ArrayBuffer
import scalaz.effect.IO

/**
 * The PaginatedQuery companion object supplies apply methods that will create new
 * PaginatedQuery's and execute them to get Streams of results.
 *
 * PaginatedQuery provides two pagination methods: 
 *  - Using LIMIT and OFFSET
 *  - Allowing the user to specify the next query based on the last record in the previous page
 *
 * The latter method is provided because the LIMIT/OFFSET method has poor performance when result
 * sets get large.
 */
object PaginatedQuery {
  /**
   * Create a new PaginatedQuery with user supplied queries, execute it, and return a Stream over
   * the results. It should be noted that the PaginatedQuery makes absolutely no changes to the
   * supplied query, so users should make sure to include LIMIT and conditional statements in the
   * query.
   * @param parser the RowParser that will parse records from the database
   * @param getNextStmt a function that will, optionally given the last record in a page of results,
   * produce a query object that can be executed to get the next page of results. The last record 
   * Option will be None when getting the first page of results.
   * @param connection the connection to use to make the query
   * @return a Stream over all the records returned by the query, getting a new page of results
   * when the current one is exhausted
   */
  def apply[A](parser: RowParser[A])(getNextStmt: Option[A] => Sql)(implicit connection: Connection): IO[Stream[A]] = {
    new PaginatedQuery(parser, connection).withQuery(getNextStmt)
  }

  /**
   * Create a new PaginatedQuery that uses LIMIT and OFFSET, execute it, and return a Stream over
   * the results.
   * @param parser the RowParser that will parse records from the database
   * @param limit the number of records each page will contain
   * @param startingOffset the offset to start with
   * @param query the Sql object to use for the query. This object should already have all
   * parameters substituted into it
   * @param connection the connection to use to make the query
   * @return a Stream over all the records returned by the query, getting a new page of results
   * when the current one is exhausted
   */
  def apply[A](parser: RowParser[A], limit: Int, startingOffset: Long)(query: Sql)(implicit connection: Connection): IO[Stream[A]] = {
    new PaginatedQuery(parser, connection).withLimitAndOffset(limit, startingOffset, query)
  }
}

/**
 * A query object that will execute a query in a paginated format and return the results in a Stream
 */
private[relate] class PaginatedQuery[A](parser: RowParser[A], connection: Connection) {

  /**
   * Create a lazily evaluated stream of results
   * @param lastRecord the last record of the previous page
   * @param getNextStmt a function that will take the last record of the previous page
   * and return a new statement to get the next page of results
   * @return a stream of results
   */
  private def withQuery(getNextStmt: Option[A] => Sql): IO[Stream[A]] = {
    /**
     * Get the next page of results
     * @param lastRecord the last record of the previous page
     * @return a stream of the records in the page
     */
    def page(lastRecord: Option[A]): IO[ArrayBuffer[A]] = {
      val sql = getNextStmt(lastRecord)
      implicit val c = connection
      sql.asCollection[A, ArrayBuffer](parser)
    }

    /**
     * Recursively create a lazily calculated list of records
     * @param lastRecord the last record of the previous page
     * @return a stream of records
     */
    def records(lastRecord: Option[A]): IO[Stream[A]] = {
      for {
        currentPage <- page(lastRecord)
        rec <- records(Some(currentPage.last))
      } yield {
        if (!currentPage.isEmpty)
          currentPage.toStream #::: rec
        else
          Stream.Empty
      }
    }

    records(None)
  }

  /**
   * Paginate results of a query by using LIMIT and OFFSET.
   * @param limit the number of records for a page
   * @param startingOffset the offset to start querying at
   * @param query the Sql object to use as the query (should have all parameters substituted in already)
   * @return whatever the callback returns
   */
  private def withLimitAndOffset(limit: Int, startingOffset: Long, query: Sql): IO[Stream[A]] = {
    val queryParams = query.queryParams
    val queryString = query.queryParams.query
    /**
     * Get the next page of results
     * @param offset how much to offset into the results
     * @return a stream of the records in the page
     */
    def page(offset: Long): IO[Stream[A]] = {
      val newParams = queryParams.copy(query = queryString + " LIMIT " + limit + " OFFSET " + offset)
      for {
        res <- NormalStatementPreparer(newParams, connection).execute(_.asIterable(parser))
      } yield (res.toStream)
    }

    /**
     * Create a lazily evaluated stream of results 
     * @param offset the offset into the database results
     * @return a stream of results
     */
    def records(offset: Long): IO[Stream[A]] = {
      for {
        currentPage <- page(offset)
        rec <- records(offset + limit)
      } yield {
        if (!currentPage.isEmpty) {
          currentPage #::: rec
        } else
          Stream.Empty
      }
    }

    records(startingOffset)
  }
}