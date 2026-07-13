package com.canvasstudio.data.local.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\f2\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0016\u0010\u000e\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\n\u00a8\u0006\u0010"}, d2 = {"Lcom/canvasstudio/data/local/dao/BlockDao;", "", "clearCanvas", "", "projectId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBlock", "block", "Lcom/canvasstudio/data/local/entity/BlockEntity;", "(Lcom/canvasstudio/data/local/entity/BlockEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBlocksForProject", "Lkotlinx/coroutines/flow/Flow;", "", "insertBlock", "updateBlock", "app_debug"})
@androidx.room.Dao()
public abstract interface BlockDao {
    
    @androidx.room.Query(value = "SELECT * FROM canvas_blocks WHERE projectId = :projectId")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.canvasstudio.data.local.entity.BlockEntity>> getBlocksForProject(long projectId);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM canvas_blocks WHERE projectId = :projectId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearCanvas(long projectId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}