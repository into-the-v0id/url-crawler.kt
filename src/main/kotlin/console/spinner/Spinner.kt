package console.spinner

import console.spinner.drawer.Drawer
import kotlinx.coroutines.*

class Spinner(val drawer: Drawer) {
    suspend fun <T> run(block: suspend () -> T): T {
        val value = withContext(Dispatchers.Default) {
            val spinnerJob = launch {
                while (true) {
                    drawer.clear()
                    drawer.draw()
                    drawer.sleep()
                }
            }

            val value = block()
            spinnerJob.cancel()
            drawer.clear()

            value
        }

        return value
    }
}
