---
layout: main
title: Home
---

relate
======

Relate is a lightweight database access layer for Scala that simplifies database interaction while leaving complete control over the actual SQL query. It was developed at [Lucid Software](https://www.golucid.co/) in response to a need for increasingly performant database access software and seeks to abstract away the PreparedStatement's idiosyncrasies while maintaining its speed.

### Advantages – Why Use Relate?

* Works with all JDBC connection types
* Works with all database engines
* Performance

### Constraints – What Relate Doesn't Do

* Create connections to databases
* ORM abstraction
 
### Use Relate - Add the Dependency

To use Relate in your own project, just include the Relate dependency in your build file:

```scala
libraryDependencies += "com.lucidchart" %% "relate" % "1.3"
```

### Basics – Writing Queries and Inserting Parameters

The core action in using Relate is writing SQL queries that contain named parameters and then calling functions to replace those parameters with their values. Here's a simple example to start with:

```scala
import com.lucidchart.open.relate._
import com.lucidchart.open.relate.Query._

SQL("""
  UPDATE pokemon
  SET move2={move}
  WHERE id={id} AND name={name}
""").on { implicit query =>
  int("id", 25)
  string("name", "Pikachu")
  string("move", "Thundershock")
}.executeUpdate()(connection)

```

This code snippet is faintly similar to using PreparedStatement. One must first write a SQL query, which is done in Relate by passing the query to the SQL function as a string. Placeholders in the query are specified by placing a parameter name between curly braces.

Next, the `on` method is used to insert values into query parameters. Parameter insertion is performed in an anonymous function passed to `on`. Within the function, [methods with names corresponding to parameter type](wiki/Parameter-Insertion-Methods) are called with parameter name and value as arguments. For convenience, if the query object is declared as an implicit parameter and there is an import for `com.lucidchart.open.relate.Query._` in scope, insertion methods can be called directly without calling them on the query object.

Finally, executing the query requires a `java.sql.Connection` parameter in a second parameter list. If the Connection is in the implicit scope, it does not need to be provided to the execute method. All queries defined in PreparedStatement (execute, executeInsert, executeQuery, executeUpdate) are also defined in Relate.

WARNING: Because Relate uses curly braces as parameter delimiters, curly braces that do not denote the start and end of parameter names should be escaped by doubling the character ("{{" and "}}"). Here's an example of such a query:

```scala
SQL("""
  UPDATE pack
  SET items= "[{{\"name\" : \"Pokeball\", \"quantity\" : 2}}]"
""")
```

### Next Steps

* [Retrieve Data from the Database](wiki/Parsers)
	* [Parsers](wiki/Parsers#defining-a-parser)
	* [Single Column Parsers](wiki/Parsers#single-column-parsers)
	* [Single Value Parsers](wiki/Parsers#single-value-parsers)
	* [Auto Increment Values](wiki/Parsers#retrieving-auto-increment-values-on-insert)
	* [Data Extraction Methods](wiki/Data-Extraction-Methods)
* [Expand Queries with .expand](wiki/Query-Expansion)
	* [Comma Separated Lists and the IN clause](wiki/Query-Expansion#commaSeparated)
	* [Tuples and Multiple Insertions](wiki/Query-Expansion#tupled-and-ontuples)
	* [Parameter Insertion Methods](wiki/Parameter-Insertion-Methods)
