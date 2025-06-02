http4k-supabase-with-sql-and-jwt-ssr-example
============================================

Dream stack.

* Supabase — can't beat their value proposition (auth, object store, scaling, REST, graphQL, subscriptions), and just fallback to Postgres in case it does not fit.
* Kotlin — Beautiful language that anyone with experience in typed OO (Java, C#, C++, modern PHP/Ruby/Python) can learn in a weekend. Lift on Java's ecosystem for heavy-lifting. Better culture than Java wrt type-safety.
* http4k — The same model Rails+Rack (Ruby), or Axum+Tower (Rust) or <many other> use. Best web framework model, well implemented in Kotlin. Very modular. Very pluggable.
* JTE/KTE — Best HTML templating system on the JVM.
* Krouton — reversible routes for type-safe link/URL building using route definitions.
* JDBI or terpal-sql — t.b.d. (thin layer on top of the industry standards JDBC and HikariCP)

This project shows:

* Authentication and authorization the Supabase way, directly with SQL from the backend code, by a `Filter` (http4k concept meaning "for all endpoints that build on top").
* Quick developer cycles.
* Pretty error pages. Useful error pages in development.
* Total control of the db queries being made. 100% PGSQL.
* Full control over the use of db transactions (nesting of transactions is allowed).
