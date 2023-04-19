package console.spinner.drawer

import java.io.PrintStream

class NullDrawer: Drawer {
    override fun clear(out: PrintStream) {}
    override fun draw(out: PrintStream) {}
    override fun getSleepDurationInMilliSeconds(): Int = 0
}
