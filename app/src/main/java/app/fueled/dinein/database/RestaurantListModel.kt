package app.fueled.dinein.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This is the class annotated with @Entity to specify
 * that this is a Room table
 * Provide a @PrimaryKey to the table
 */

@Entity
data class RestaurantListModel(
    @PrimaryKey
    val id: String
)