package console.spinner.drawer

import kotlinx.coroutines.delay
import java.io.PrintStream

class NullDrawer: Drawer {
    override fun clear(out: PrintStream) {}
    override fun draw(out: PrintStream) {}
    override suspend fun sleep() = delay(500)
}
