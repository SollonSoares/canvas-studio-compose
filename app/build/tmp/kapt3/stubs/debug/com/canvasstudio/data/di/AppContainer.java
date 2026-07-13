package com.canvasstudio.data.di;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\t\u00a8\u0006\n"}, d2 = {"Lcom/canvasstudio/data/di/AppContainer;", "", "blockRepository", "Lcom/canvasstudio/data/repository/BlockRepository;", "getBlockRepository", "()Lcom/canvasstudio/data/repository/BlockRepository;", "projectRepository", "Lcom/canvasstudio/data/repository/ProjectRepository;", "getProjectRepository", "()Lcom/canvasstudio/data/repository/ProjectRepository;", "app_debug"})
public abstract interface AppContainer {
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.canvasstudio.data.repository.ProjectRepository getProjectRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.canvasstudio.data.repository.BlockRepository getBlockRepository();
}