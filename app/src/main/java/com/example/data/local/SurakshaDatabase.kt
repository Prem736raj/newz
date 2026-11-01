package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.SavedCaseOfflineEntity
import com.example.data.model.TranslationCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCaseOfflineDao {
    @Query("SELECT * FROM saved_cases_offline ORDER BY savedAtMillis DESC")
    fun observeAllSavedCases(): Flow<List<SavedCaseOfflineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedCase(entity: SavedCaseOfflineEntity)

    @Query("DELETE FROM saved_cases_offline WHERE caseId = :caseId")
    suspend fun deleteSavedCaseById(caseId: String)

    @Query("SELECT * FROM saved_cases_offline WHERE caseId = :caseId LIMIT 1")
    suspend fun getSavedCaseById(caseId: String): SavedCaseOfflineEntity?
}

@Dao
interface TranslationCacheDao {
    @Query("SELECT * FROM translation_cache WHERE langCode = :langCode")
    fun observeTranslationsForLanguage(langCode: String): Flow<List<TranslationCacheEntity>>

    @Query("SELECT * FROM translation_cache WHERE langCode = :langCode")
    suspend fun getTranslationsForLanguage(langCode: String): List<TranslationCacheEntity>

    @Query("SELECT text FROM translation_cache WHERE contentId = :contentId AND langCode = :langCode AND field = :field LIMIT 1")
    suspend fun getTranslatedField(contentId: String, langCode: String, field: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTranslation(entity: TranslationCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTranslations(entities: List<TranslationCacheEntity>)
}

@Database(
    entities = [SavedCaseOfflineEntity::class, TranslationCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SurakshaDatabase : RoomDatabase() {
    abstract fun savedCaseOfflineDao(): SavedCaseOfflineDao
    abstract fun translationCacheDao(): TranslationCacheDao

    companion object {
        @Volatile
        private var INSTANCE: SurakshaDatabase? = null

        fun getInstance(context: Context): SurakshaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SurakshaDatabase::class.java,
                    "suraksha_chronicle_offline.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}
