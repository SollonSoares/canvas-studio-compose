package com.canvasstudio.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH&J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\r"}, d2 = {"Lcom/canvasstudio/data/repository/ProjectRepository;", "", "deleteProject", "", "project", "Lcom/canvasstudio/data/local/entity/ProjectEntity;", "(Lcom/canvasstudio/data/local/entity/ProjectEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProjects", "Lkotlinx/coroutines/flow/Flow;", "", "insertProject", "", "updateProject", "app_debug"})
public abstract interface ProjectRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.canvasstudio.data.local.entity.ProjectEntity>> getAllProjects();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}