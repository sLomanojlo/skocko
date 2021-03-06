package com.sloman.rs.skocko.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sloman.rs.skocko.util.Converter
import com.sloman.rs.skocko.viewmodel.GameViewModel
import com.sloman.rs.skocko.util.ioThread
import com.sloman.rs.skocko.model.Game
import com.sloman.rs.skocko.model.Guess
import java.security.SecureRandom

/** [Database] class for declaring, instantiating, and prepopulating the [GameDatabase].*/
@Database(
    entities = [Game::class, Guess::class],
    version = 1,
    exportSchema = false)
@TypeConverters(Converter::class)
abstract class GameDatabase : RoomDatabase() {

    abstract val gameDao : GameDao

    companion object {

        private const val DB_NAME = "game_DB"

                @Volatile
                private var INSTANCE: GameDatabase? = null

                fun getInstance(context: Context): GameDatabase =
                    INSTANCE ?: synchronized(this) {
                        INSTANCE
                            ?: create(
                                context
                            ).also { INSTANCE = it }
                    }

                    private fun create(context: Context): GameDatabase {
                        return Room.databaseBuilder(
                            context,
                            GameDatabase::class.java,
                            DB_NAME
                        )
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    // insert the data on the IO Thread
                                    ioThread {
                                        getInstance(
                                            context
                                        ).gameDao.insertOnlyGame(
                                            Game(
                                                0,
                                                createRandomArray(),
                                                ""
                                            )
                                        )
                                    }
                                }
                            })
                            .build()
                    }

        /**Create random array of Int using [SecureRandom] lib. */
        fun createRandomArray(): List<Int> {

            return listOf(
                SecureRandom().nextInt(GameViewModel.SYMBOL_NO),
                SecureRandom().nextInt(GameViewModel.SYMBOL_NO),
                SecureRandom().nextInt(GameViewModel.SYMBOL_NO),
                SecureRandom().nextInt(GameViewModel.SYMBOL_NO)
            )
        }

    }
}