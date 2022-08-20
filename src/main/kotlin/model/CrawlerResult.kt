package model

data class CrawlerResult(
    var urls: List<ProcessedUrl>,
    var failedUrls: List<FailedUrl>,
)
