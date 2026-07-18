package com.github.anrimian.musicplayer.data.database

import android.content.Context
import androidx.room.Room
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider

/**
 * Created on 18.11.2017.
 */
class DatabaseManager(
    private val context: Context,
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider,
) {

    fun getLibraryDatabase(): LibraryDatabase {
        return Room.databaseBuilder(
            context,
            LibraryDatabase::class.java,
            LIBRARY_DATABASE_NAME
        ).addMigrations(
                Migrations.getMigration1_2(context),
                Migrations.MIGRATION_2_3,
                Migrations.getMigration3_4(),
                Migrations.MIGRATION_4_5,
                Migrations.MIGRATION_5_6,
                Migrations.MIGRATION_6_7,
                Migrations.MIGRATION_7_8,
                Migrations.MIGRATION_8_9,
                Migrations.MIGRATION_9_10,
                Migrations.MIGRATION_10_11,
                Migrations.MIGRATION_11_12,
                Migrations.MIGRATION_12_13,
                Migrations.getMigration13_14(context),
                Migrations.MIGRATION_14_15,
                Migrations.getMigration15_16(context),
                Migrations.MIGRATION_16_17,
                Migrations.MIGRATION_17_18,
                Migrations.getMigration18_19(context, systemAudioCatalogProvider),
                Migrations.MIGRATION_19_20
            )
            .build()
    }

    fun getConfigsDatabase(): ConfigsDatabase {
        return Room.databaseBuilder(
            context,
            ConfigsDatabase::class.java,
            CONFIGS_DATABASE_NAME
        ).addMigrations(
            Migrations.MIGRATION_CONFIG_1_2
        ).build()
    }

    private companion object {
        const val LIBRARY_DATABASE_NAME = "music_player_database"
        const val CONFIGS_DATABASE_NAME = "configs_database"
    }

}
