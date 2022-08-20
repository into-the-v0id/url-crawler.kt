import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import model.CrawlerResult
import model.FailedUrl
import model.ProcessedUrl
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URL

fun HttpStatusCode.isRedirect(): Boolean = value in (300 until 400)

class UrlCrawler(private val http: HttpClient) {
    private var pendingUrls: MutableList<Url> = mutableListOf()
    private var processingUrls: MutableList<Url> = mutableListOf()
    private var processedUrls: MutableList<ProcessedUrl> = mutableListOf()
    private var failedUrls: MutableList<FailedUrl> = mutableListOf()
    private val stateMutex: Mutex = Mutex()

    suspend fun crawlRecursive(url: Url): CrawlerResult {
        clear()
        scanRecursive(url)

        return getResult()
    }

    suspend fun getResult(): CrawlerResult {
        return stateMutex.withLock {
            CrawlerResult(
                processedUrls.toList(),
                failedUrls.toList(),
            )
        }
    }

    suspend fun clear() {
        stateMutex.withLock {
            pendingUrls.clear()
            processingUrls.clear()
            processedUrls.clear()
            failedUrls.clear()
        }
    }

    suspend fun scanRecursive(url: Url) {
        scan(url)

        val nextUrls = stateMutex.withLock {
            if (pendingUrls.isEmpty()) return

            val nextUrls = pendingUrls.toList()
            pendingUrls.clear()
            processingUrls.addAll(nextUrls)
            nextUrls
        }

        withContext(Dispatchers.Default) {
            nextUrls.forEach {
                launch { scanRecursive(it) }
            }
        }
    }

    suspend fun scan(url: Url) {
        val processedUrl = try {
            process(url)
        } catch (e: Throwable) {
            failedUrls.add(FailedUrl(
                url,
                "${e::class.qualifiedName}: ${e.message}"
            ))
            return
        }

        stateMutex.withLock {
            processedUrls.add(processedUrl)

            val newUrls = processedUrl.linkUrls
                .filter { it.host == url.host }
                .filter { linkUrl ->
                    linkUrl !in pendingUrls
                            && linkUrl !in processingUrls
                            && processedUrls.none { processedUrl -> processedUrl.url == linkUrl }
                            && failedUrls.none { failedUrl -> failedUrl.url == linkUrl }
                }

            pendingUrls.addAll(newUrls)
        }
    }

    private suspend fun process(url: Url): ProcessedUrl {
        val response = http.get(url)

        return ProcessedUrl(
            url,
            response.status,
            findLinkUrls(response),
        )
    }

    private suspend fun findLinkUrls(response: HttpResponse): List<Url> {
        if (response.status.isRedirect()) {
            val locationHeader = response.headers["Location"]
            if (! locationHeader.isNullOrEmpty()) {
                val redirectUrl = Url(URL(response.request.url.toURI().toURL(), locationHeader).toURI())

                return listOf(redirectUrl)
            }
        }

        return when (response.contentType()?.withoutParameters()) {
            ContentType.Text.Html -> findLinkUrlsFromHtml(response)
            else -> emptyList()
        }
    }

    private suspend fun findLinkUrlsFromHtml(response: HttpResponse): List<Url> {
        val doc = Jsoup.parse(
            response.body<InputStream>(),
            response.charset()?.toString() ?: "utf-8",
            response.request.url.toString(),
        )

        return doc.getElementsByTag("a")
            .asSequence()
            .map { it.absUrl("href") }
            .filter { it.startsWith("http://") || it.startsWith("https://") }
            .distinct()
            .map { normalizeUrl(Url(it)) }
            .distinct()
            .toList()
    }

    private fun normalizeUrl(url: Url): Url {
        val builder = URLBuilder(url)
        builder.fragment = ""

        return builder.build()
    }
}
