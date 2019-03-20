package app.fueled.dinein.models

/**
 * @author Annsh Singh
 *
 * This model class hierarchy is designed according to the Foursquare API structure
 */
data class FeedModel(
    val meta: MetaModel,
    val response: ResponseModel
)

