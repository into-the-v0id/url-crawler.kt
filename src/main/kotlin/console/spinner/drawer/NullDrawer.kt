package console.spinner.drawer

import java.io.PrintStream

class NullDrawer: Drawer {
    override fun clear(out: PrintStream) {}
    override fun drawInProgress(out: PrintStream) {}
    override fun drawSuccess(out: PrintStream) {}
    override fun drawFailure(out: PrintStream) {}
    override fun getSleepDurationInMilliSeconds(): Int = 0
}
