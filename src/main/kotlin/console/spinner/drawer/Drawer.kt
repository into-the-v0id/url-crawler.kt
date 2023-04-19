package console.spinner.drawer

import java.io.PrintStream

interface Drawer {
    fun clear(out: PrintStream)
    fun draw(out: PrintStream)
    suspend fun sleep()
}
