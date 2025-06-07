http4k-supabase-with-sql-and-jwt-ssr-example
============================================

Dream stack.

* Supabase — Can't beat their value proposition (auth, object store, scaling, REST, graphQL, subscriptions), and just fallback to Postgres in case it does not fit.
* Kotlin — Beautiful language that anyone with typed OO experience (Java, C#, C++, modern PHP/Ruby/Python) can learn in a weekend. Lift on Java's ecosystem for heavy-lifting. Better culture than Java wrt type-safety.
* http4k — The same architectural model as Rails+Rack (Ruby), or Axum+Tower (Rust) or <many other> use, well implemented in as a bunch of libs Kotlin. Very modular. Very pluggable.
* kotlinx.html — Write HTML templates in Kotlin's DSL syntax, adds type safety where possible.
* JDBI — Conveniently keep queries in separate SQL files. Does not interfere with SQL code at all (not an ORM or query builder), so 100% "just SQL".

### Authentication

We use JWTs for authentication. Since this is primarily a server side rendered application, the rotating refresh token scheme is not useful for us.
Therefor we use long lived JWT access tokens for authentication (a `session_id` does exist, but it is not exposed by the auth service).
These tokens are valid for 100 hours (about half a week) and will be rotated on first use after the first 50 hours of the expiry time have passed.

This requires a the following setting in Supabase's `conifg.toml`:

```toml
jwt_expiry = 360000
```


### Project goals

* Authentication and authorization the Supabase way, directly with SQL from the backend code, by a `Filter` (http4k concept meaning "for all endpoints that build on top").
* Quick developer cycles.
* Pretty error pages. Useful error pages in development.
* Total control of the db queries being made. 100% PGSQL.
* Full control over the use of db transactions (nesting of transactions is allowed), and no transaction is started automatically per request cycle.
* 12 factor principles (like: configuration by env vars).

