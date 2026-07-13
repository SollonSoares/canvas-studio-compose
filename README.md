# 🎨 Canvas Studio Native (Naruto RPG)

Estúdio nativo e reativo de desenho interativo e gerenciamento de fichas shinobi desenvolvido em **Kotlin** e **Jetpack Compose**.

---

## 📑 1. Diretrizes de Engenharia e Arquitetura

Este projeto foi concebido a partir da transição de uma arquitetura Web imperativa (HTML5 Canvas/Vanilla JS) para um ecossistema declarativo nativo de alto desempenho. A nova estrutura adota o padrão **MVVM (Model-View-ViewModel)** com fluxo unidirecional de dados (UDF).

### 📂 Estrutura Estrita de Diretórios

``text
app/src/main/java/com/canvasstudio/
├── core/
│   ├── data/
│   │   ├── AppDatabase.kt          # Camada Room DB (Substitui DB.js - IndexedDB)
│   │   └── LocalPreferences.kt     # Camada DataStore (Substitui LocalStorage)
│   ├── viewmodel/
│   │   └── CanvasViewModel.kt      # State Management Central (Substitui App.js/EventBus)
│   └── util/
│       └── ModifierExtensions.kt   # Extensões de UI (Substitui CanvasManager.js)
├── ui/
│   ├── theme/
│   │   ├── Color.kt                # Paleta de Cores Hexadecimal (Substitui style.css)
│   │   └── Theme.kt                # Definição Material Theme Dark/Light
│   ├── components/
│   │   ├── NavigationSidebar.kt    # Menu Lateral Colapsável (Substitui <aside> no index.html)
│   │   ├── CanvasStage.kt          # Palco Infinito de Design (Substitui <main> no index.html)
│   │   └── SettingsModal.kt        # Painel de Controle e Módulos (Substitui #settings-modal)
│   └── modules/
│       ├── ChartWidget.kt          # Renderizador Poligonal (Substitui ChartModule.js)
│       ├── ImageWidget.kt          # Carregador de Mídias (Substitui ImageModule.js)
│       ├── TextWidget.kt           # Editor WYSIWYG Nativo (Substitui TextModule.js)
│       └── PortabilityEngine.kt    # Pipeline de Backup (Substitui PortabilityModule.js)
└── model/
    └── CanvasEntities.kt           # Contratos e Modelos de Dados (Substitui BaseModule.js)
