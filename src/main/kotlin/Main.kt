/*
 * Copyright (C) Oliver Amann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import console.spinner.Spinner
import console.spinner.drawer.LineDrawer
import console.spinner.drawer.TextDrawer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.validator.routines.UrlValidator
import kotlin.system.exitProcess

object Http {
    val client = HttpClient(OkHttp) {
        followRedirects = false
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("error: missing arguments")
        println("usage: url-crawler URL")
        exitProcess(1)
    }

    if (args.size > 1) {
        System.err.println("error: unexpected arguments")
        println("usage: url-crawler URL")
        exitProcess(1)
    }

    val rawStartUrl = args.first()
    val isValidStartUrl = UrlValidator(UrlValidator.ALLOW_LOCAL_URLS + UrlValidator.ALLOW_2_SLASHES)
        .isValid(rawStartUrl)
    if (! isValidStartUrl) {
        System.err.println("error: invalid URL")
        println("usage: url-crawler URL")
        exitProcess(1)
    }

    val startUrl = Url(rawStartUrl)

    val crawlerResult = runBlocking {
        val drawer = if (Spinner.isFancyTerminal) { LineDrawer("Scanning URLs") }
            else { TextDrawer("Scanning URLs") }
        val spinner = Spinner(drawer)

        spinner.run {
            UrlCrawler(Http.client)
                .crawlRecursive(startUrl)
        }
    }

    if (crawlerResult.failedUrls.isNotEmpty()) {
        println()
        println("Failed URLs:")
        crawlerResult.failedUrls.forEach { println("[${it.url}] ${it.message}") }
    }

    if (crawlerResult.urls.isNotEmpty()) {
        println()
        println("Results:")
        crawlerResult.urls
            .groupBy { it.statusCode }
            .forEach { (statusCode, processedUrls) ->
                println("HTTP ${statusCode.value}: ${processedUrls.size}")
            }
    }

    Http.client.close()

    if (crawlerResult.failedUrls.isNotEmpty()) {
        exitProcess(1)
    }
}
