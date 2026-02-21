package com.example.projet_android_m2

import android.content.Context
import android.os.Bundle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.OnPlacedModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader

// Test structure basic jsonL
@Serializable
data class Payload(
    val id : Long,
    val name : Name,
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
    val name : Name,
    val location : List<Double>,
    val zone : Boolean
)

// Test code jsondecoder kotlinlang

// Class representing Either<Left|Right>
sealed class Either {
    data class Left(val errorMsg: String) : Either()
    data class Right(val data: Payload) : Either()
}

// Objet Kserializer<Either>, avec 2 fonction internes deserialize / serialize,
// Serializer injects custom behaviour by inspecting object content and writing
object EitherSerializer : KSerializer<Either> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("com.example.projet_android_m2.Either", PolymorphicKind.SEALED) {
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

fun JsonRead(context: Context): String {
    // check securite bug
        val json = Json { ignoreUnknownKeys = true }
        return try {
            // ouvre le fichier Json
            val jsonFile = context.assets.open("people-places.jsonl")
            val bufferReader = BufferedReader(InputStreamReader(jsonFile))

            val line = bufferReader.readLine()
            var outputRes = ""

            if (line != null) {
                // Convertie la ligne en JsonElement
                val elementJson = json.parseToJsonElement(line)

                // verifie si erreur dans le payload
                val resultat: Either
                val erreur = elementJson.jsonObject["error"]?.jsonPrimitive?.content
                if (erreur != null) {
                    resultat = Either.Left(erreur)
                } else {
                    resultat =
                        Either.Right(json.decodeFromJsonElement(Payload.serializer(), elementJson))
                }
                outputRes = resultat.toString()
                println("Résultat : $resultat")
            } else {
                outputRes = "Fichier vide ou probleme"
            }
            bufferReader.close()
            outputRes
        } catch (e: Exception){
            "Erreur: ${e.message}"
        } as String
}

@Composable
@Preview(showBackground = true)
fun JsonDeroulo(modifier: Modifier = Modifier){
    val context = LocalContext.current
    val resultat = JsonRead(context)
    Text(
        text = "Contenu du JSONL : \n $resultat",
        modifier = modifier
    )
}