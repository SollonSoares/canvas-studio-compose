package com.canvasstudio.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000f2\u0006\u0010\u0007\u001a\u00020\bH\u0016J\u0016\u0010\u0011\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/canvasstudio/data/repository/BlockRepositoryImpl;", "Lcom/canvasstudio/data/repository/BlockRepository;", "blockDao", "Lcom/canvasstudio/data/local/dao/BlockDao;", "(Lcom/canvasstudio/data/local/dao/BlockDao;)V", "clearCanvas", "", "projectId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBlock", "block", "Lcom/canvasstudio/data/local/entity/BlockEntity;", "(Lcom/canvasstudio/data/local/entity/BlockEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBlocksForProject", "Lkotlinx/coroutines/flow/Flow;", "", "insertBlock", "updateBlock", "app_debug"})
public final class BlockRepositoryImpl implements com.canvasstudio.data.repository.BlockRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.canvasstudio.data.local.dao.BlockDao blockDao = null;
    
    public BlockRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.dao.BlockDao blockDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.canvasstudio.data.local.entity.BlockEntity>> getBlocksForProject(long projectId) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object clearCanvas(long projectId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}