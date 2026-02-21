package com.example.projet_android_m2

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// Test structure basic jsonL
@Serializable
data class Payload(
    val id : Long,
    val name : String,
    val places : List<Place>
)
@Serializable
data class Name(
    val en : String,
    val fr : String
)

@Serializable
data class Place(
    val id : Long,
    val name : String,
    val location : List<Double>,
    val zone : Boolean
)

// Test code jsondecoder kotlinlang

// Class representing Either<Left|Right>
sealed class Either {
    data class Left(val errorMsg: String) : Either()
    data class Right(val data: Payload) : Either()
}

// Serializer injects custom behaviour by inspecting object content and writing
object EitherSerializer : KSerializer<Either> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("package.Either", PolymorphicKind.SEALED) {
        // ..
    }

    override fun deserialize(decoder: Decoder): Either {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This class can be decoded only by Json format")
        val tree = input.decodeJsonElement() as? JsonObject ?: throw SerializationException("Expected JsonObject")
        if ("error" in tree) return Either.Left(tree["error"]!!.jsonPrimitive.content)
        return Either.Right(input.json.decodeFromJsonElement(Payload.serializer(), tree))
    }

    override fun serialize(encoder: Encoder, value: Either) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This class can be encoded only by Json format")
        val tree = when (value) {
            is Either.Left -> JsonObject(mapOf("error" to JsonPrimitive(value.errorMsg)))
            is Either.Right -> output.json.encodeToJsonElement(Payload.serializer(), value.data)
        }
        output.encodeJsonElement(tree)
    }
}
