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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

object Http {
    val client = HttpClient(OkHttp) {
        followRedirects = false
    }
}

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()

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

    val startUrl = Url(args.first())

    val crawlerResult = runBlocking {
        Spinner(LineDrawer("Scanning URLs"))
            .run {
                UrlCrawler(Http.client)
                    .crawlRecursive(startUrl)
            }.also { println("✓ Scanning URLs") }
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

    val processTime = System.currentTimeMillis() - startTime
    println()
    println("Completed in $processTime ms")

    if (crawlerResult.failedUrls.isNotEmpty()) {
        exitProcess(1)
    }
}
