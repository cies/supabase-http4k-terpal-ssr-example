package lib.blockingterpal

import io.exoquery.sql.Action
import io.exoquery.sql.ActionReturning
import io.exoquery.sql.BatchAction
import io.exoquery.sql.BatchActionReturning
import io.exoquery.sql.Query
import io.exoquery.sql.TerpalDriver
import kotlinx.coroutines.runBlocking

// Copied from [io.exoquery.sql.TerpalDriver], added `runBlocking { ... }`, removed the `suspend` keywords.

fun <T> Query<T>.runOn(ctx: TerpalDriver) = runBlocking { ctx.run(this@runOn) }
fun <T> Query<T>.streamOn(ctx: TerpalDriver) = runBlocking { ctx.stream(this@streamOn) }
fun <T> Query<T>.runRawOn(ctx: TerpalDriver) = runBlocking { ctx.runRaw(this@runRawOn) }
fun Action.runOn(ctx: TerpalDriver) = runBlocking { ctx.run(this@runOn) }
fun <T> ActionReturning<T>.runOn(ctx: TerpalDriver) = runBlocking { ctx.run(this@runOn) }
fun BatchAction.runOn(ctx: TerpalDriver) = runBlocking { ctx.run(this@runOn) }
fun <T> BatchActionReturning<T>.runOn(ctx: TerpalDriver) = runBlocking { ctx.run(this@runOn) }
fun <T> BatchActionReturning<T>.streamOn(ctx: TerpalDriver) = runBlocking { ctx.stream(this@streamOn) }
