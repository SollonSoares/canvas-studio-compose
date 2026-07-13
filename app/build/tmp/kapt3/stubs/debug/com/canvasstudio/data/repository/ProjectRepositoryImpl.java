package com.canvasstudio.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0014\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000bH\u0016J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/canvasstudio/data/repository/ProjectRepositoryImpl;", "Lcom/canvasstudio/data/repository/ProjectRepository;", "projectDao", "Lcom/canvasstudio/data/local/dao/ProjectDao;", "(Lcom/canvasstudio/data/local/dao/ProjectDao;)V", "deleteProject", "", "project", "Lcom/canvasstudio/data/local/entity/ProjectEntity;", "(Lcom/canvasstudio/data/local/entity/ProjectEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProjects", "Lkotlinx/coroutines/flow/Flow;", "", "insertProject", "", "updateProject", "app_debug"})
public final class ProjectRepositoryImpl implements com.canvasstudio.data.repository.ProjectRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.canvasstudio.data.local.dao.ProjectDao projectDao = null;
    
    public ProjectRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.dao.ProjectDao projectDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.canvasstudio.data.local.entity.ProjectEntity>> getAllProjects() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}