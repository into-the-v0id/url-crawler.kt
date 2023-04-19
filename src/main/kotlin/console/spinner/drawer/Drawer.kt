package console.spinner.drawer

import java.io.PrintStream

interface Drawer {
    fun clear(out: PrintStream)
    fun drawInProgress(out: PrintStream)
    fun drawSuccess(out: PrintStream)
    fun drawFailure(out: PrintStream)
    fun getSleepDurationInMilliSeconds(): Int
}
