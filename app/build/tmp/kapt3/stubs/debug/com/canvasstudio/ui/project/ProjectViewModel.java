package com.canvasstudio.ui.project;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0012"}, d2 = {"Lcom/canvasstudio/ui/project/ProjectViewModel;", "Landroidx/lifecycle/ViewModel;", "projectRepository", "Lcom/canvasstudio/data/repository/ProjectRepository;", "(Lcom/canvasstudio/data/repository/ProjectRepository;)V", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/canvasstudio/ui/project/ProjectUiState;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "deleteProject", "", "project", "Lcom/canvasstudio/data/local/entity/ProjectEntity;", "insertProject", "name", "", "Companion", "app_debug"})
public final class ProjectViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.canvasstudio.data.repository.ProjectRepository projectRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.canvasstudio.ui.project.ProjectUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.canvasstudio.ui.project.ProjectViewModel.Companion Companion = null;
    
    public ProjectViewModel(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.repository.ProjectRepository projectRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.canvasstudio.ui.project.ProjectUiState> getUiState() {
        return null;
    }
    
    public final void insertProject(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void deleteProject(@org.jetbrains.annotations.NotNull()
    com.canvasstudio.data.local.entity.ProjectEntity project) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/canvasstudio/ui/project/ProjectViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_debug"})
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