package console.spinner.drawer

import kotlinx.coroutines.delay

class NullDrawer: Drawer {
    override fun clear() {}
    override fun draw() {}
    override suspend fun sleep() = delay(500)
}
