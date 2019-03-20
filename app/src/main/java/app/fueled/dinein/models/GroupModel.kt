package app.fueled.dinein.models

data class GroupModel (
    val items: ArrayList<VenueItemModel>
)

data class VenueItemModel(
    val venue: VenueModel
)

data class VenueModel(
    val id: String,
    val name: String,
    val location: LocationModel,
    val categories: ArrayList<CategoryModel>
)

data class LocationModel(
    val address: String,
    val crossStreet: String,
    val lat: String,
    val lng: String,
    val distance: Int,
    val formattedAddress: ArrayList<String>,
    val city: String
)

data class CategoryModel(
    val id: String,
    val name: String,
    val icon: IconModel
)

data class IconModel(
    val prefix: String,
    val suffix: String
)

