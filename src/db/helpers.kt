package db

import io.exoquery.sql.jdbc.TerpalDriver
import java.time.Instant
import java.time.OffsetDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/** An often used type. Aliased to shorten it. */
typealias DbCtx = TerpalDriver.Postgres


//
// Types with `@Serializable(with = ...)` annotations. Aliasing them with their annotation cleans up the code.
//

typealias DbInstant = @Serializable(with = InstantToPgTimestamptzSerializer::class) Instant
object InstantToPgTimestamptzSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("import java.time.Instant", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Instant =
    OffsetDateTime.parse(decoder.decodeString().replace(" ", "T")).toInstant()

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeString(value.toString().replace(" ", "T"))
  }
}
