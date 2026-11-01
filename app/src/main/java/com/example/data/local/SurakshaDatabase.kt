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

@Database(entities = [SavedCaseOfflineEntity::class], version = 1, exportSchema = false)
abstract class SurakshaDatabase : RoomDatabase() {
    abstract fun savedCaseOfflineDao(): SavedCaseOfflineDao

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
