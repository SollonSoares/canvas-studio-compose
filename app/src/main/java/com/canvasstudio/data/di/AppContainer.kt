// src/main/java/com/canvasstudio/data/di/AppContainer.kt
interface AppContainer {
    val blockDao: com.canvasstudio.data.local.dao.BlockDao
}

class AppContainerImpl(context: android.content.Context) : AppContainer {
    private val database = androidx.room.Room.databaseBuilder(
        context,
        com.canvasstudio.data.local.AppDatabase::class.java,
        "canvas_db"
    ).build()

    override val blockDao = database.blockDao()
}