package com.lucidchart.open.relate

import java.sql.Connection
import scalaz.effect.IO

private[relate] object Relate {

  /**
   * Create a SqlQuery object from a SQL statement
   * @param stmt the SQL statement
   * @return the corresponding SqlQuery object
   */
  private[relate] def sql(stmt: String) = IO { ExpandableQuery(stmt) }

}