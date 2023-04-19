package console.spinner

import console.spinner.drawer.Drawer
import kotlinx.coroutines.*
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicBoolean

class Spinner(private val drawer: Drawer) {
    suspend fun <T> run(block: suspend () -> T): T = withContext(Dispatchers.Default) {
        hookOutputStream { oldOut, newOut ->
            val inProgress = AtomicBoolean(true)

            val spinnerJob = launch {
                newOut.subscribeToBeforeWrite { drawer.clear(oldOut) }
                newOut.subscribeToAfterWrite { drawer.draw(oldOut) }

                while (inProgress.get()) {
                    drawer.draw(oldOut)
                    drawer.sleep()
                    drawer.clear(oldOut)
                }
            }

            val response = block()

            inProgress.set(false)
            spinnerJob.join()

            response
        }
    }

    private suspend fun <R> hookOutputStream(block: suspend (oldOut: PrintStream, newOut: ObserverOutputStream) -> R): R {
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
