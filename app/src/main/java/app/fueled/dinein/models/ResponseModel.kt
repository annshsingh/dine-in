package app.fueled.dinein.models

data class ResponseModel (
    val totalResults: Int,
    val groups: ArrayList<GroupModel>
)