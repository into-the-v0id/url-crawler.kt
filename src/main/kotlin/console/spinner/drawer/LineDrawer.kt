package console.spinner.drawer

import kotlinx.coroutines.delay

class LineDrawer(val label: String? = null): Drawer {
    companion object Data {
        val states: List<Char> = listOf('|', '/', '-', '\\')
    }

    private var index: Int = 0

    override fun clear() {
        print("\u001B[2K\r \r")
        System.out.flush()
    }

    override fun draw() {
        print("${states[index]} $label")
        System.out.flush()

        index += 1
        if (index >= states.size) {
            index = 0
        }
    }

    override suspend fun sleep() = delay(200)
}
