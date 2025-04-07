package grapes.microservices.models.data

import java.util.Date

data class Opinion (
    val userfullname: String,
    val message: String,
    val dateTime: Date,
)
