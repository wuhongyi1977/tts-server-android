package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.model.LocalTtsEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.Locale
import java.security.MessageDigest
import kotlin.math.abs
class LocalTtsViewModel : ViewModel() {
    private val engine by lazy { LocalTtsEngine(App.context) }

    val engines = mutableStateListOf<TextToSpeech.EngineInfo>()
    val locales = mutableStateListOf<Locale>()
    val voices = mutableStateListOf<Voice>()

    fun init() {
        engines.clear()
        engines.addAll(LocalTtsEngine.getEngines())
    }

    suspend fun setEngine(engine: String) {
        val ok = this.engine.setEngine(engine)
        if (!ok) return

        engines.clear()
        engines.addAll(LocalTtsEngine.getEngines())
    }

    fun updateLocales() {
        locales.clear()
        locales.addAll(engine.locales)
    }

    fun updateVoices(locale: String) {
        voices.clear()
        voices.addAll(engine.voices
            .filter { it.locale.toLanguageTag() == locale }
            .sortedBy { it.name }
        )
    }

    override fun onCleared() {
        super.onCleared()

        engine.shutdown()
    }
    fun generateUniqueIntID(input: String): Int {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val hashCode = bytes.fold(0) { acc, byte -> (acc * 31 + byte.toInt()) }
        return abs(hashCode)
    }
    fun generateJsonFromVoices(groupId: Int): String {
        val jsonObject = buildJsonObject {
            put("group", buildJsonObject {
                put("id", groupId)
                put("name", "旁白对话BGM")
                put("order", 1)
                put("isExpanded", true)
            })
            put("list", buildJsonArray {
                var isGenNarration = false
                var isGenDialogueStandby = false
                voices.forEachIndexed { index, voice ->
                    if (voice.name.indexOf("NOT_SET") == -1) {
                        val featureStr =
                            if (voice.features == null || voice.features.isEmpty()) "" else voice.features.toString()
                        if (!isGenNarration) {
                            val displayName2 = "旁白 ${voice.name} [$featureStr]"
                            val id2 = generateUniqueIntID(displayName2)
                            addJsonObject {
                                put("id", id2)
                                put("groupId", groupId)
                                put("displayName", displayName2)
                                put("isEnabled", true)
                                put("speechRule", buildJsonObject {
                                    put("target", 4)
                                    put("isStandby", false)
                                    put("tag", "narration")
                                    put("tagRuleId", "ttsrv.multi_voice")
                                    put("tagName", "旁白")
                                })
                                put("tts", buildJsonObject {
                                    put("#type", "local")
                                    put("engine", "org.nobody.multitts")
                                    put("locale", voice.locale.toLanguageTag())
                                    put("voiceName", voice.name)
                                    putJsonObject("audioFormat") {}
                                })
                                put("order", index)
                            }
                            isGenNarration = true
                        }

                        val displayName = "${voice.name} [$featureStr]"
                        val id = generateUniqueIntID(displayName)
                        addJsonObject {
                            put("id", id)
                            put("groupId", groupId)
                            put("displayName", displayName)
                            put("isEnabled", true)
                            put("speechRule", buildJsonObject {
                                put("target", 4)
                                put("isStandby", !isGenDialogueStandby)
                                put("tag", "dialogue")
                                put("tagRuleId", "ttsrv.multi_voice")
                                put("tagName", "对话")
                            })
                            put("tts", buildJsonObject {
                                put("#type", "local")
                                put("engine", "org.nobody.multitts")
                                put("locale", voice.locale.toLanguageTag())
                                put("voiceName", voice.name)
                                putJsonObject("audioFormat") {}
                            })
                            put("order", index + 1)
                        }
                        if(!isGenDialogueStandby)
                            isGenDialogueStandby = true
                    }
                }
            })
        }

        return Json.encodeToString(JsonObject.serializer(), jsonObject)
    }
}