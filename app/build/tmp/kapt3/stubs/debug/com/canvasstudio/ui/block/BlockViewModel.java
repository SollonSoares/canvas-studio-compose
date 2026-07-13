package com.canvasstudio.ui.block;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0007J\u000e\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0014\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0007J\u000e\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0012R\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0017"}, d2 = {"Lcom/canvasstudio/ui/block/BlockViewModel;", "Landroidx/lifecycle/ViewModel;", "blockRepository", "Lcom/canvasstudio/data/repository/BlockRepository;", "(Lcom/canvasstudio/data/repository/BlockRepository;)V", "_currentProjectId", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/canvasstudio/ui/block/BlockUiState;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearCanvas", "", "projectId", "deleteBlock", "block", "Lcom/canvasstudio/data/local/entity/BlockEntity;", "insertBlock", "setProject", "updateBlock", "Companion", "app_debug"})
@kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
public final class BlockViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.canvasstudio.data.repository.BlockRepository blockRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _currentProjectId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.canvasstudio.ui.block.BlockUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.canvasstudio.ui.block.BlockViewModel.Companion Companion = null;
    
    public BlockViewModel(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.repository.BlockRepository blockRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.canvasstudio.ui.block.BlockUiState> getUiState() {
        return null;
    }
    
    public final void setProject(long projectId) {
    }
    
    public final void insertBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block) {
    }
    
    public final void updateBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block) {
    }
    
    public final void deleteBlock(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.BlockEntity block) {
    }
    
    public final void clearCanvas(long projectId) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/canvasstudio/ui/block/BlockViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final androidx.lifecycle.ViewModelProvider.Factory getFactory() {
            return null;
        }
    }
}