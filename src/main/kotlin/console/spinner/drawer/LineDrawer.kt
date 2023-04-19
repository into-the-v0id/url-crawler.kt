package console.spinner.drawer

import java.io.PrintStream

class LineDrawer(val label: String): Drawer {
    companion object Data {
        val states: List<Char> = listOf('|', '/', '-', '\\')
    }

    private var index: Int = 0

    override fun clear(out: PrintStream) {
        out.print("\u001B[0K")
        out.flush()
    }

    private fun draw(out: PrintStream, prefix: String) {
        out.print("\u001B[s")
        out.print("$prefix $label")
        out.print("\u001B[u")
        out.flush()
    }

    override fun drawInProgress(out: PrintStream) {
        draw(out, prefix = states[index].toString())

        index += 1
        if (index >= states.size) {
            index = 0
        }
    }

    override fun drawSuccess(out: PrintStream) = draw(out, prefix = "✓")

    override fun drawFailure(out: PrintStream) = draw(out, prefix = "✗")

    override fun getSleepDurationInMilliSeconds(): Int = 200
}
