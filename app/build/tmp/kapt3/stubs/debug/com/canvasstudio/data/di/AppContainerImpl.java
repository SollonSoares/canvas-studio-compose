package com.canvasstudio.data.di;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u001b\u0010\u000b\u001a\u00020\f8VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\n\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0010"}, d2 = {"Lcom/canvasstudio/data/di/AppContainerImpl;", "Lcom/canvasstudio/data/di/AppContainer;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "database", "Lcom/canvasstudio/data/local/AppDatabase;", "getDatabase", "()Lcom/canvasstudio/data/local/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "projectRepository", "Lcom/canvasstudio/data/repository/ProjectRepository;", "getProjectRepository", "()Lcom/canvasstudio/data/repository/ProjectRepository;", "projectRepository$delegate", "app_debug"})
public final class AppContainerImpl implements com.canvasstudio.data.di.AppContainer {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy projectRepository$delegate = null;
    
    public AppContainerImpl(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final com.canvasstudio.data.local.AppDatabase getDatabase() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.canvasstudio.data.repository.ProjectRepository getProjectRepository() {
        return null;
    }
}