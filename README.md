# 🎨 Canvas Studio Native (Naruto RPG)

Estúdio nativo e reativo de desenho interativo e gerenciamento de fichas shinobi desenvolvido em **Kotlin** e **Jetpack Compose**.

---

## 📑 1. Diretrizes de Engenharia e Arquitetura

Este projeto foi concebido a partir da transição de uma arquitetura Web imperativa (HTML5 Canvas/Vanilla JS) para um ecossistema declarativo nativo de alto desempenho. A nova estrutura adota o padrão **MVVM (Model-View-ViewModel)** com fluxo unidirecional de dados (UDF).

### 📂 Estrutura Estrita de Diretórios

```text
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
```
## 🔄 2. Matriz de Equivalência Técnica (Web vs. Nativo)

### 2.1 Camada de Persistência e Banco de Dados Local
*   **Web Origem (`src/core/DB.js`):** Implementava um wrapper imperativo para gerenciamento assíncrono baseado em Promises da API IndexedDB do navegador para o armazenamento de metadados e mídias binárias pesadas[cite: 3].
*   **Compose Destino (`core/data/AppDatabase.kt`):** Transiciona para a biblioteca **Room Database** operando sob SQLite estável. Garante integridade referencial via compilação tipada estável, controle transacional nativo via Kotlin Coroutines e segurança contra falhas durante operações simultâneas de IO em segundo plano.

### 2.2 Alinhamento Magnético e Manipulação Geométrica
*   **Web Origem (`src/core/CanvasManager.js`):** Monitorava o evento de clique e movimento do mouse (`mousemove`, `mouseup`), realizando o truncamento matemático e inserindo coordenadas absolutas de pixel (`px`) de forma imperativa diretamente na árvore de estilos do elemento DOM[cite: 6].
*   **Compose Destino (`core/util/ModifierExtensions.kt`):** A lógica matemática de *Snap-to-Grid* é convertida em um modificador customizado e reutilizável (`Modifier.snapToGrid(gridSize)`). Os cálculos de arraste capturados pelo ponteiro de entrada (`pointerInput`) utilizam a densidade de pixels local (`LocalDensity`) do dispositivo, operando em valores abstratos de densidade (`Dp`) e mantendo a interface isolada da escala física da tela.

### 2.3 Motor Trigonométrico de Renderização Gráfica
*   **Web Origem (`src/modules/chart/ChartModule.js`):** Injetava uma tag `<canvas>` no DOM[cite: 8], utilizando o contexto 2D para limpar a tela (`clearRect`)[cite: 8] e redesenhar manualmente os eixos radiais dos atributos de Naruto RPG (NIN, INT, CHK, TAI, VIG, GEN) atrelados a loops contínuos de ponteiro[cite: 8].
*   **Compose Destino (`ui/modules/ChartWidget.kt`):** Utiliza o componente nativo `Canvas` integrado à engine gráfica **Skia**. O loop de renderização do polígono de status shinobi passa a ser reativo: o gráfico só executa o redesenho se as variáveis numéricas dos atributos sofrerem modificação em nível de estado, eliminando overhead processual da CPU e transferindo o desenho vetorial diretamente para pipelines de hardware da GPU.

### 2.4 Motor de Dimensionamento e Ciclo de Vida
*   **Web Origem (`src/modules/resize/ResizeModule.js`):** Injetava alças manuais de dimensionamento em blocos arrastáveis através de um `MutationObserver` global acoplado ao palco, monitorando adições manuais no DOM para evitar falhas assíncronas do ciclo de vida[cite: 11].
*   **Compose Destino (`ui/modules/ResizableWidgetContainer`):** A estrutura de dimensionamento deixa de depender de observadores de mutação externos. Os limites e dimensões do contêiner são propriedades mutáveis armazenadas em memória e ligadas de forma declarativa ao ciclo de composição. Caso o bloco seja removido da árvore reativa, o ecossistema desaloca seus recursos automaticamente de forma limpa na memória RAM.

---

## 🚀 3. Tecnologias Utilizadas
*   **Linguagem:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material Design 3)
*   **Asynchrony:** Kotlin Coroutines & Flows
*   **Local DB:** Room Database (SQLite)
*   **Preferences:** Jetpack DataStore
*   **Serialization:** Kotlinx Serialization
*   **Image Loading:** Coil Image Loader
