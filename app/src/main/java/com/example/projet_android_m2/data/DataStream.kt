package com.example.projet_android_m2.data

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    // si valeur manquante on met null
    val en : String?= "Null",
    val fr : String?= "Null"
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
    override val descriptor: SerialDescriptor = buildSerialDescriptor("com.example.projet_android_m2.data.Either", PolymorphicKind.SEALED) {
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

fun JsonRead(context: Context, numeroPage: Int, taillePage: Int = 10): List<String> {
    // check securite bug
        val json = Json { ignoreUnknownKeys = true }
        val outputRes = mutableListOf<String>()
        val debut = numeroPage * taillePage
        val fin = debut + taillePage

        return try {
            // ouvre le fichier Json
            val jsonFile = context.assets.open("people-places.jsonl")
            val bufferReader = BufferedReader(InputStreamReader(jsonFile))

            var indexLigne = 0
            // linesequence important pour parcourir le fichier sans charger les 900mo d'un coup !
            for (line in bufferReader.lineSequence()){
                if (indexLigne >= debut && indexLigne < fin) {
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
                    outputRes.add(resultat.toString())
                }
                if (indexLigne >= fin-1) {
                    break
                }
                indexLigne++
            }
            bufferReader.close()
            outputRes
        } catch (e: Exception){
            listOf("Erreur: ${e.message}")
        }
}

@Composable
@Preview(showBackground = true)
fun JsonDeroulo(modifier: Modifier = Modifier){
    val context = LocalContext.current
    var listJson by remember { mutableStateOf(listOf("Chargement...")) }
    // Coroutine simple ici pour pas bloquer le chargement du json
    LaunchedEffect(Unit) {
        val resultat = JsonRead(
            context = context,
            numeroPage = 0,
            taillePage = 4 // test sur 4
        )
        listJson = resultat
    }

    Column(modifier = modifier) {
        Text(
            text = "Contenu du JSONL :",
            modifier = modifier
        )
        listJson.forEach { item ->
            Text(text = item, modifier = Modifier.padding(4.dp))
        }
    }
}