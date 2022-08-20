package console.spinner.drawer

interface Drawer {
    fun clear()
    fun draw()
    suspend fun sleep()
}
