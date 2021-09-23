package com.ymy.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ymy.core.database.dao.UserInfoDao
import com.ymy.core.user.UserInfoDB

/**
 * The Room database for this app
 */
@Database(entities = [UserInfoDB::class], version = 3, exportSchema = false)
abstract class AppUserDatabase : RoomDatabase() {
    abstract fun userInfoDao(): UserInfoDao

    companion object {
        @Volatile
        private var instance: AppUserDatabase? = null

        fun getInstance(context: Context): AppUserDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppUserDatabase {
            return Room.databaseBuilder(context, AppUserDatabase::class.java, DATABASE_NAME_USER_V2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                .build()
        }
        //升级语句
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_info_v2 ADD COLUMN imId TEXT NOT NULL DEFAULT \"\"")
            }
        }

        //升级语句
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_info_v2 ADD COLUMN imSign TEXT NOT NULL DEFAULT \"\"")
            }
        }

//        //升级语句
//        private val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE user_info ADD COLUMN token TEXT NOT NULL DEFAULT \"\"")
//                database.execSQL("ALTER TABLE user_info ADD COLUMN token TEXT NOT NULL DEFAULT \"\"")
//                database.execSQL("ALTER TABLE user_info ADD COLUMN companyId INTEGER NOT NULL DEFAULT 0")
//                database.execSQL("ALTER TABLE user_info ADD COLUMN companies TEXT NOT NULL DEFAULT \"\"")
//            }
//        }
    }
}
