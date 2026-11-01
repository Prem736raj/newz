package com.example.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.example.data.model.LokSabhaDataset
import com.example.data.model.RajyaSabhaDataset
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MpsRepository(
    private val assetLoader: suspend () -> Pair<String, String>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(
        assetLoader = {
            val assets: AssetManager = context.applicationContext.assets
            val lokJson = assets.open(LOK_SABHA_ASSET_FILE).bufferedReader().use { it.readText() }
            val rajyaJson = assets.open(RAJYA_SABHA_ASSET_FILE).bufferedReader().use { it.readText() }
            lokJson to rajyaJson
        },
        ioDispatcher = ioDispatcher
    )

    private val mutex = Mutex()
    private var cachedLokSabha: LokSabhaDataset? = null
    private var cachedRajyaSabha: RajyaSabhaDataset? = null

    suspend fun loadDatasets(forceReload: Boolean = false): Pair<LokSabhaDataset, RajyaSabhaDataset> =
        withContext(ioDispatcher) {
            mutex.withLock {
                val existingLok = cachedLokSabha
                val existingRajya = cachedRajyaSabha
                if (!forceReload && existingLok != null && existingRajya != null) {
                    return@withLock existingLok to existingRajya
                }

                val (lokSabhaRaw, rajyaSabhaRaw) = assetLoader()
                val lokDataset = parseLokSabhaJson(lokSabhaRaw)
                val rajyaDataset = parseRajyaSabhaJson(rajyaSabhaRaw)

                cachedLokSabha = lokDataset
                cachedRajyaSabha = rajyaDataset
                lokDataset to rajyaDataset
            }
        }

    companion object {
        const val LOK_SABHA_ASSET_FILE = "lok_sabha_mps.json"
        const val RAJYA_SABHA_ASSET_FILE = "rajya_sabha_mps.json"

        val jsonParser: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        fun parseLokSabhaJson(rawJson: String): LokSabhaDataset =
            jsonParser.decodeFromString(LokSabhaDataset.serializer(), rawJson)

        fun parseRajyaSabhaJson(rawJson: String): RajyaSabhaDataset =
            jsonParser.decodeFromString(RajyaSabhaDataset.serializer(), rawJson)
    }
}
