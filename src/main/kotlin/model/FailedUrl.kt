package model

import io.ktor.http.*

data class FailedUrl(
    val url: Url,
    val message: String,
)
