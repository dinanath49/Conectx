package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        CallLogEntity::class,
        NotificationItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ConnectXDatabase : RoomDatabase() {
    abstract fun dao(): ConnectXDao

    companion object {
        @Volatile
        private var INSTANCE: ConnectXDatabase? = null

        fun getDatabase(context: Context): ConnectXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConnectXDatabase::class.java,
                    "connectx_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
