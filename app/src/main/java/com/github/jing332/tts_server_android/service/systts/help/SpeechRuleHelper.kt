package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings.Secure
import android.util.Log
import com.github.jing332.tts_server_android.constant.ReplaceExecution
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.github.jing332.tts_server_android.model.speech.TtsTextSegment
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.ui.AppHelpDocumentActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Random

class SpeechRuleHelper {
    private val random: Random by lazy { Random(SystemClock.elapsedRealtime()) }
    lateinit var engine: SpeechRuleEngine

    fun init(context: Context, rule: SpeechRule) {
        engine = SpeechRuleEngine(context, rule)
        engine.eval()
        random.setSeed(SystemClock.elapsedRealtime())
        mTextReplacer.load()
    }

    fun splitText(text: String): List<String> {
        return engine.splitText(text).map { it.toString() }
    }
    var aiTextCache :MutableMap<String,String> = mutableMapOf()
    var bookRoleTTSMap: Map<String, Map<String, ITextToSpeechEngine>> = mutableMapOf()
    val mTextReplacer = TextReplacer()
    var multiTTSStr = ""
    var preChatText = ""
    var fetching = false
    var skipCount = 0
    fun handleText(
        text: String,
        bound: android.os.Bundle?,
        config: Map<String, List<ITextToSpeechEngine>>,
        defaultConfig: ITextToSpeechEngine,
    ): List<TtsTextSegment> {
        if (!this::engine.isInitialized) return listOf(TtsTextSegment(defaultConfig, text))
        if (text.isBlank()) return listOf(TtsTextSegment(defaultConfig, text))

        val resultList = mutableListOf<TtsTextSegment>()
        if(bound?.getString("tts_bookName").isNullOrBlank()){
            val list = config.entries.map { it.value }.flatten().map { it.speechRule }
            engine.handleText(text, list).forEach { txtWithTag ->
                if (txtWithTag.text.isNotBlank()) {
                    val sameTagList = config[txtWithTag.tag] ?: listOf(defaultConfig)
                    val ttsFromId = sameTagList.find { it.speechRule.configId == txtWithTag.id }

                    val tts = ttsFromId ?: sameTagList[random.nextInt(sameTagList.size)]
                    resultList.add(TtsTextSegment(text = txtWithTag.text, tts = tts))
                }
            }
        }else {
            try {
                val bookName = bound?.getString("tts_bookName")?:""
                val bookAuthor = bound?.getString("tts_bookAuthor")?:""
                val curTitle = bound?.getString("tts_curTitle")?:""

                // 收集设备信息
                val androidId =
                    Secure.getString(engine.context.getContentResolver(), Secure.ANDROID_ID)
                val deviceModel = Build.MODEL
                // 组合信息
                val deviceInfo = androidId + deviceModel
                val id = deviceInfo
                val groupName = bookName + "_" + bookAuthor
                val rangeText = bound?.getString("tts_curLongText")?:""
                val rangeText2 = bound?.getString("tts_nextLongText")?:""
                var nextTitle = bound?.getString("tts_nextTitle")?:""
                if(nextTitle.isBlank())
                    nextTitle = curTitle

                if (multiTTSStr == "") {
                    multiTTSStr = " "
                    config["dialogue"]?.forEach { tts ->
                        if (tts.speechRule.tagRuleId == "ttsrv.multi_voice" && tts.displayName?.indexOf(
                                "在线"
                            ) == -1
                        )
                            multiTTSStr += "${tts.speechRule.configId}_i&d_${tts.displayName}_r&d_"
                    }
                    val ruleGroup = appDb.replaceRuleDao.getGroupByName(groupName)
                    if (multiTTSStr == " ")
                        multiTTSStr = ""
                    else {
                        GlobalScope.launch {
                            if (ruleGroup == null) {
                                try {
                                    val rep =
                                        tts_server_lib.Tts_server_lib.fetchRelpaceRuler(
                                            id,
                                            bookName,
                                            bookAuthor,
                                            curTitle,
                                            multiTTSStr,
                                            ""
                                        )
                                    if (rep.isNotBlank())
                                        mTextReplacer.insertFromJson(rep)
                                } catch (e: Exception) {
                                    Log.d("SpeechRuleHelper", "Caught exception: ${e.message}")
                                }
                                multiTTSStr = ""
                            } else {
                                if (nextTitle != curTitle) {
                                    resultList.add(
                                        TtsTextSegment(
                                            text = "智能语音运行中，由胖胖的老鼠提供。",
                                            tts = (config["narration"] ?: listOf(defaultConfig))[0]
                                        )
                                    )
                                    val gid = ruleGroup.id
                                    try {
                                        val rep =
                                            tts_server_lib.Tts_server_lib.fetchNewRelpaceRuler(
                                                id,
                                                bookName,
                                                bookAuthor,
                                                curTitle,
                                                multiTTSStr,
                                                gid.toString()
                                            )
                                        if (rep.isNotBlank())
                                            mTextReplacer.addFromJson(rep)
                                    } catch (e: Exception) {
                                        Log.d("SpeechRuleHelper", "Caught exception: ${e.message}")
                                        multiTTSStr = ""
                                    }
                                }
                                multiTTSStr = ""
                            }
                        }
                    }
                }

                Log.d(AppHelpDocumentActivity.TAG, "handleText: $text")
                var aiRangeText: String = ""
                if (skipCount > 4) {
                    skipCount = 0
                    fetching = false
                }

                if (rangeText.isNotBlank() ?: false) {
                    if (aiTextCache.contains(rangeText)) {
                        aiRangeText = aiTextCache[rangeText] ?: ""
                        if (rangeText2.isNotBlank() && (!aiTextCache.contains(rangeText2))) {
                            if (fetching) {
                                skipCount++
                            } else {
                                var aiRangeText2 =""
                                GlobalScope.launch {
                                    fetching = true
                                    try {
                                        aiRangeText2 =
                                            tts_server_lib.Tts_server_lib.fetchAIChat(
                                                id,
                                                bookName,
                                                bookAuthor,
                                                nextTitle,
                                                rangeText2,
                                                preChatText
                                            )
                                        if (aiRangeText2.isNotBlank()) {
                                            if (aiRangeText2.contains('【') && (!rangeText2.contains('【')))
                                                aiRangeText2.replace('【', '[')
                                            if (aiRangeText2.contains('】') && (!rangeText2.contains('】')))
                                                aiRangeText2.replace('】', ']')
                                            aiTextCache[rangeText2] = aiRangeText2
                                            preChatText = aiRangeText2
                                        }
                                    } catch (e: Exception) {
                                        Log.d("SpeechRuleHelper", "Caught exception: ${e.message}")
                                    }

                                    fetching = false
                                }
                            }
                        }
                    } else {
                        if (fetching) {
                            skipCount++
                        } else {
                            GlobalScope.launch {
                                fetching = true
                                try {
                                    val rep =
                                        tts_server_lib.Tts_server_lib.fetchAIChat(
                                            id,
                                            bookName,
                                            bookAuthor,
                                            curTitle,
                                            rangeText,
                                            preChatText
                                        )
                                    if (rep.isNotBlank()) {
                                        if (rep.contains('【') && (!rangeText.contains('【')))
                                            rep.replace('【', '[')
                                        if (rep.contains('】') && (!rangeText.contains('】')))
                                            rep.replace('】', ']')
                                        aiTextCache[rangeText] = rep
                                        preChatText = rep
                                    }
                                } catch (e: Exception) {
                                    Log.d("SpeechRuleHelper", "Caught exception: ${e.message}")
                                }

                                fetching = false
                            }
                        }
                    }
                }

                val list = config.entries.map { it.value }.flatten().map { it.speechRule }
                val handleList = engine.handleText(text, list)
                val replaceTag = ""
                var emotion = ""

                handleList.forEach { txtWithTag ->
                    if (txtWithTag.text.isNotBlank()) {
                        val sameTagList = config[txtWithTag.tag] ?: listOf(defaultConfig)
                        val tts = if ("dialogue" == txtWithTag.tag && aiRangeText.isNotBlank()) {
                            val index = aiRangeText.indexOf(txtWithTag.text)
                            if (index != -1) {
                                val p = aiRangeText.indexOf('[', index)
                                val l = aiRangeText.indexOf(']', index)
                                if (p >= 0 && l >= 0 && p < l) {
                                    // sex_name:tone
                                    var tagText = aiRangeText.substring(p + 1, l)

                                    Log.d("SpeechRuleHelper", "handleList.replace $tagText")
                                    val replaced =
                                        mTextReplacer.replace(tagText, ReplaceExecution.TAG) { }
                                    Log.d("SpeechRuleHelper", "handleList.replaced $replaced")
                                    val parts = replaced.split("_", ":")
                                    if(parts.isNotEmpty())
                                        emotion = parts[parts.size - 1]

                                    parts.firstNotNullOfOrNull { part ->
                                        sameTagList.find {
                                            (it.speechRule.tagData["role"]?.equals(part) == true) || it.speechRule.configId.toString()
                                                .equals(part)
                                        }
                                    }
                                } else {
                                    Log.d(
                                        "SpeechRuleHelper",
                                        "Invalid brackets found after index: $index"
                                    )
                                    null
                                }
                            } else {
                                Log.d("SpeechRuleHelper", "Text not found in aiRangeText")
                                Log.d("SpeechRuleHelper", "text: $aiRangeText")
                                Log.d("SpeechRuleHelper", txtWithTag.text)
//                                (config["narration"] ?: sameTagList)[0]
                                null
                            }
                        } else {
                            val ttsFromId =
                                sameTagList.find { it.speechRule.configId == txtWithTag.id }
//                            ttsFromId ?: sameTagList[0]
                            ttsFromId
                        }
                        val role = tts?.speechRule?.tagData?.get("role") ?: tts?.displayName
                        Log.d("SpeechRuleHelper", "role: $role")
                        if(tts == null && resultList.size >= 1){
                            resultList[resultList.size - 1].text += txtWithTag.text
                        }else {
                            resultList.add(
                                TtsTextSegment(
                                    text = txtWithTag.text,
                                    tts = tts ?: (config["narration"] ?: sameTagList)[0],
                                    emotion = emotion
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // 处理超时或其他错误
                Log.e("TTS", "Error fetching replace ruler", e)
                Log.d("SpeechRuleHelper", "Exception text: $text")
                return listOf(TtsTextSegment(defaultConfig, text))
            }
        }
        return resultList
    }

}