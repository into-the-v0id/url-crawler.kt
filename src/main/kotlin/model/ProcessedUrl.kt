package model

import io.ktor.http.*

data class ProcessedUrl(
    val url: Url,
    val statusCode: HttpStatusCode,
    val linkUrls: List<Url>,
)
