package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ContentItem

@Database(entities = [ContentItem::class], version = 1, exportSchema = false)
abstract class CreatorForgeDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        @Volatile
        private var INSTANCE: CreatorForgeDatabase? = null

        fun getDatabase(context: Context): CreatorForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CreatorForgeDatabase::class.java,
                    "creator_forge.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
