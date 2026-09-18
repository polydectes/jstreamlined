# JStreamlined

JStreamlined is the Spring Boot/Thymeleaf successor to the original Ruby on Rails
Streamlined UI. It connects to an existing database and builds a schema model at
runtime instead of generating ORM classes.

## Run

Copy `config/datasource.properties.example` to `config/datasource.properties`,
set the vendor, host, port, database, username, password, and optional schema,
then run:

```sh
mvn spring-boot:run
```

Supported JDBC vendors are PostgreSQL, MySQL, Oracle, and SQL Server. The
connection form accepts username, password, database name, and schema, then
loads tables, columns, indexes, and imported-key relationships via JDBC
`DatabaseMetaData`.
