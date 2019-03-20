package app.fueled.dinein.models

/**
 * @author Annsh Singh
 *
 * This model class hierarchy is designed according to the Foursquare API structure
 */

data class DetailsModel(
    val meta: MetaModel,
    val response: DetailsResponseModel
)

data class DetailsResponseModel(
    val venue: VenueDetailsModel
)

data class VenueDetailsModel(
    val contact: ContactModel?,
    val canonicalUrl: String?,
    val description: String?,
    val hours: HoursModel?
)

data class ContactModel(
    val phone: String?
)

data class HoursModel(
    val timeframes: ArrayList<TimeFrameModel>?
)

data class TimeFrameModel(
    val days: String?,
    val open: ArrayList<OpenModel>?
)

data class OpenModel(
    val renderedTime: String?
)

