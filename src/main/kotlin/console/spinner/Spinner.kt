package console.spinner

import console.spinner.drawer.Drawer
import kotlinx.coroutines.*
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicBoolean

class Spinner(private val drawer: Drawer) {
    companion object {
        val isFancyTerminal = System.getenv("TERM")?.contains("xterm") ?: false
    }

    suspend fun <T> run(block: suspend () -> T): T {
        try {
            return process(block)
                .also { success() }
        } catch (e: Throwable) {
            failure()
            throw e
        }
    }

    suspend fun <T> process(block: suspend () -> T): T = withContext(Dispatchers.Default) {
        hookOutputStream { oldOut, newOut ->
            val inProgress = AtomicBoolean(true)

            val spinnerJob = launch {
                newOut.subscribeToBeforeWrite { drawer.clear(oldOut) }
                newOut.subscribeToAfterWrite { drawer.drawInProgress(oldOut) }

                while (inProgress.get()) {
                    drawer.drawInProgress(oldOut)

                    var sleepDuration = drawer.getSleepDurationInMilliSeconds()
                    while (sleepDuration > 0 && inProgress.get()) {
                        if (sleepDuration < 10) {
                            delay(sleepDuration.toLong())
                            sleepDuration = 0
                            continue
                        }

                        delay(10)
                        sleepDuration -= 10
                    }

                    drawer.clear(oldOut)
                }
            }

            val response = block()

            inProgress.set(false)
            spinnerJob.join()

            response
        }
    }

    fun success() {
        drawer.drawSuccess(System.out)
        System.out.appendLine()
    }

    fun failure() {
        drawer.drawFailure(System.out)
        System.out.appendLine()
    }

    private suspend fun <R> hookOutputStream(
        block: suspend (oldOut: PrintStream, newOut: ObserverOutputStream) -> R
    ): R {
        val oldOutputStream = System.out
        val newOutputStream = ObserverOutputStream(oldOutputStream)
        System.setOut(PrintStream(newOutputStream))

        try {
            return block(oldOutputStream, newOutputStream)
        } finally {
            System.setOut(oldOutputStream)
        }
    }
}
