package app.fueled.dinein.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.fueled.dinein.models.VenueItemModel


/**
 * @author Annsh Singh
 *
 * @entities - All the entity classes (an array)
 * @version - Database version. To be updated everytime the schema changes
 *
 * This class is used to create the database and get an instance of it.
 *
 * List all the Daos here
 */

@Database(
    entities = [RestaurantListModel::class], version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {

    companion object {
        private var instance: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            if (instance == null) instance =
                Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "dinein_database")
                    .fallbackToDestructiveMigration()
                    .build()
            return instance!!
        }
    }

    abstract fun getIsDislikedDao(): IsDislikedDao
}

