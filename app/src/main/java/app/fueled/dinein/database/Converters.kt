package app.fueled.dinein.database

import androidx.room.TypeConverter
import app.fueled.dinein.models.VenueModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * @author Annsh Singh
 *
 * This class is to provide converters for RoomDatabase tables
 */
class Converters {

    /**
     * This is to save VenueModel types in Database
     * Converts VenueModel to String
     * @param entity
     */

    @TypeConverter
    fun fromVenueModel(entity: VenueModel): String {
        val gson = Gson()
        return gson.toJson(entity)

    }

    /**
     * Converts String to VenueModel
     * @param entity
     */

    @TypeConverter
    fun fromStringToVenueModel(entity: String): VenueModel {
        val entityType = object : TypeToken<VenueModel>() {

        }.type
        return Gson().fromJson(entity, entityType)
    }
}