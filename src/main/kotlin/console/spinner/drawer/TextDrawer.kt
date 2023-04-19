package console.spinner.drawer

import java.io.PrintStream

class TextDrawer(val label: String): Drawer {
    private var hasDrawnInProgress: Boolean = false
    private var hasDrawnStatus: Boolean = false

    override fun clear(out: PrintStream) {}

    override fun drawInProgress(out: PrintStream) {
        if (hasDrawnInProgress) {
            return
        }

        out.print("$label ...")
        out.flush()

        hasDrawnInProgress = true
    }

    override fun drawSuccess(out: PrintStream) {
        if (hasDrawnStatus) {
            return
        }

        out.print(" done")
        out.flush()

        hasDrawnStatus = true
    }

    override fun drawFailure(out: PrintStream) {
        if (hasDrawnStatus) {
            return
        }

        out.print(" error")
        out.flush()

        hasDrawnStatus = true
    }

    override fun getSleepDurationInMilliSeconds(): Int = 0
}
