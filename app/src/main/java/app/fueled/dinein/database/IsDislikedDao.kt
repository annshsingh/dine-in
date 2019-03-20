package app.fueled.dinein.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * @author Annsh Singh
 *
 * This is the Data Access Object to provide methods
 * that are used to perform CRUD operations on the
 * RestaurantListModel table.
 */

@Dao
interface IsDislikedDao {

    /**
     * Method to find out if the venue id is in the table
     */

    @Query("SELECT 1 FROM RestaurantListModel WHERE id=:id LIMIT 1")
    fun isDisliked(id: String?): LiveData<Int>

    @Query("SELECT 1 FROM RestaurantListModel WHERE id=:id LIMIT 1")
    fun isDislikedSync(id: String?): Int


    /**
     * Method to Insert the venue id in the table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDisliked(id: RestaurantListModel?)

    /**
     * Method to Delete the venue id from the table
     */
    @Query("DELETE FROM RestaurantListModel WHERE id=:id ")
    fun delete(id: String?)
}