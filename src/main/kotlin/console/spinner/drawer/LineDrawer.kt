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

    override fun draw(out: PrintStream) {
        out.print("\u001B[s")
        out.print("${states[index]} $label")
        out.print("\u001B[u")
        out.flush()

        index += 1
        if (index >= states.size) {
            index = 0
        }
    }

    override fun getSleepDurationInMilliSeconds(): Int = 200
}
