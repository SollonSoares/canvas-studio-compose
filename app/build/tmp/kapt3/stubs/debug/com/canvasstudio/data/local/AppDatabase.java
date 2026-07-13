package com.canvasstudio.data.local;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lcom/canvasstudio/data/local/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "blockDao", "Lcom/canvasstudio/data/local/dao/BlockDao;", "projectDao", "Lcom/canvasstudio/data/local/dao/ProjectDao;", "app_debug"})
@androidx.room.Database(entities = {com.canvasstudio.data.local.entity.ProjectEntity.class, com.canvasstudio.data.local.entity.BlockEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.canvasstudio.data.local.dao.ProjectDao projectDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.canvasstudio.data.local.dao.BlockDao blockDao();
}