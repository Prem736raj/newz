package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CorrectionRequestEntity
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

@Dao
interface CorrectionRequestDao {
    @Query("SELECT * FROM correction_requests_offline ORDER BY createdAtMillis DESC")
    fun observeAllCorrectionRequests(): Flow<List<CorrectionRequestEntity>>

    @Query("SELECT * FROM correction_requests_offline WHERE caseId = :caseId ORDER BY createdAtMillis DESC")
    suspend fun getCorrectionRequestsForCase(caseId: String): List<CorrectionRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCorrectionRequest(entity: CorrectionRequestEntity)
}

@Database(
    entities = [
        SavedCaseOfflineEntity::class,
        TranslationCacheEntity::class,
        CorrectionRequestEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SurakshaDatabase : RoomDatabase() {
    abstract fun savedCaseOfflineDao(): SavedCaseOfflineDao
    abstract fun translationCacheDao(): TranslationCacheDao
    abstract fun correctionRequestDao(): CorrectionRequestDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `translation_cache` (
                        `contentId` TEXT NOT NULL,
                        `langCode` TEXT NOT NULL,
                        `field` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        `sourceHash` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`contentId`, `langCode`, `field`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `saved_cases_offline` ADD COLUMN `imageUrl` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `saved_cases_offline` ADD COLUMN `videoUrl` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `saved_cases_offline` ADD COLUMN `mediaCaption` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `correction_requests_offline` (
                        `requestId` TEXT NOT NULL,
                        `caseId` TEXT NOT NULL,
                        `issueType` TEXT NOT NULL,
                        `details` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`requestId`)
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: SurakshaDatabase? = null

        fun getInstance(context: Context): SurakshaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SurakshaDatabase::class.java,
                    "suraksha_chronicle_offline.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
