<div align="center">

# 🎨 CANVAS STUDIO COMPOSE
### *Next-Gen Reactive Infinite Canvas Engine & On-Device Intelligence*

<p align="center">
  <strong>O estado da arte em engenharia visual, arquitetura reativa e design industrial para Android. Uma plataforma de workspace espacial infinito com renderização acelerada por GPU, Design System de Duplo Universo (Apple vs Teenage Engineering), pipeline de IA On-Device (Google ML Kit OCR), automação de commits na nuvem (GitHub REST API) e portabilidade bidirecional de dados.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_Native-0A84FF?style=for-the-badge&logo=android&logoColor=white" alt="Android Native">
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.0">
  <img src="https://img.shields.io/badge/UI_Toolkit-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Architecture-Clean_%26_Feature--Flows-34C759?style=for-the-badge" alt="Clean Architecture">
  <img src="https://img.shields.io/badge/Intelligence-Google_ML_Kit-FF9500?style=for-the-badge&logo=google&logoColor=white" alt="ML Kit">
  <img src="https://img.shields.io/badge/Cloud_Sync-GitHub_REST_API-24292E?style=for-the-badge&logo=github&logoColor=white" alt="GitHub API">
</p>

---

### 📑 Sumário Executivo & Arquitetural
[1. Visão Geral](#-visão-geral-do-sistema) • 
[2. Design System & Duplo Universo](#-design-system-centralizado--duplo-universo-visual) • 
[3. Splash Screen Animada Cinematográfica](#-splash-screen-animada-cinematográfica) • 
[4. Arquitetura Modular por Flows](#-arquitetura-modular-por-flows) • 
[5. Motores de Engenharia & Performance GPU](#-motores-de-engenharia--performance-gpu) • 
[6. Pipeline de IA & OCR On-Device](#-pipeline-de-ia--ocr-on-device) • 
[7. Automação Cloud & Sync GitHub REST API](#-automação-cloud--sync-direto-github-rest-api) • 
[8. Portabilidade & Esquema JSON v2.0.0](#-portabilidade-e-esquema-de-dados-v200) • 
[9. Guia de Execução e Build](#-guia-de-execução-e-engenharia)

</div>

---

## 🌟 Visão Geral do Sistema

O **Canvas Studio Compose** é um ambiente de computação espacial bidimensional de alta precisão projetado para manipulação livre de blocos (texto rico com markdown, mídia de alta resolução, gráficos vetoriais multieixo e comprovantes financeiros). Construído 100% sobre **Jetpack Compose**, o sistema elimina completamente o overhead de Views legadas através de transformações diretas em GPU e um micro-kernel reativo impulsionado por Kotlin Coroutines e StateFlow.

### 💎 Principais Diferenciais Técnicos:
* **Renderização 60/120 FPS cravados**: Transformações espaciais (Pan e Zoom infinito de `0.15x` a `3.0x`) desacopladas da fase de layout via `graphicsLayer`.
* **Design System de Duplo Universo Estético**: Metamorfose completa em tempo real entre o minimalismo fluido da **Apple Cupertino (Sir Jony Ive)** e a engenharia física e tátil da **Teenage Engineering (Jesper Kouthoofd)**.
* **On-Device Vision AI (OCR)**: Análise em tempo real de comprovantes bancários (PIX, TED, Tributos, Boletos) via **Google ML Kit Vision**, com extração inteligente de valores, partes (`De`/`Para`), instituição e timestamp.
* **Automação de Commits Diretos no GitHub**: Sincronização e upload automático de imagens para o repositório [`SollonSoares/galeria`](https://github.com/SollonSoares/galeria) via GitHub Contents API, gerando URLs públicas no GitHub Pages em segundos.
* **Shield de Memória & SQLite Anti-Crash**: Eliminação do estouro de `CursorWindow` (2MB) através do salvamento atômico em disco de arquivos locais (`file:///`) com *downsampling* inteligente de fotos pesadas da câmera (`inSampleSize`).

---

## 🎨 Design System Centralizado & Duplo Universo Visual

Todo o ecossistema visual do aplicativo é orquestrado pelo pacote `com.canvasstudio.designsystem`, provido via `CompositionLocalProvider` e desacoplado de regras de negócio.

Ao alternar o tema nas **Configurações**, todo o aplicativo sofre uma **transformação estrutural de geometria, tipografia, bordas e grade**:

```
┌───────────────────────────┬────────────────────────────────────────────────────────┐
│ ATRIBUTO DE ENGENHARIA    │ 🍏 1. APPLE CUPERTINO          │ 🎛️ 2. TEENAGE ENGINEERING      │
├───────────────────────────┼────────────────────────────────┼───────────────────────┤
│ Autor Inspirador          │ Sir Jony Ive                   │ Jesper Kouthoofd      │
│ Filosofia                 │ Vidro, fluidez e pureza        │ Hardware mecânico e tátil       │
│ Tipografia                │ Sans-Serif Proporcional        │ Monospaçada / DIN Técnica      │
│ Geometria dos Blocos      │ Squircles suaves (14dp - 24dp) │ Cantos usinados retos (4dp)   │
│ Bordas dos Blocos         │ Hairline translúcida (1dp)     │ Chanfro sólido usinado (1.5dp) │
│ Grade do Canvas           │ Pontos circulares polidos      │ Cruzetas técnicas de estúdio (+)|
│ Badges Financeiros        │ Cápsulas ovais fluidas (999dp) │ Brackets de LED [ PIX ] [ R$ ] │
│ Identificador do Módulo   │ Discreto e minimalista         │ LED luminoso indicador tátil  │
│ Acento Primário           │ #0A84FF / #007AFF (Apple Blue) │ #FF4500 (OP-1 Signal Orange)  │
└───────────────────────────┴────────────────────────────────┴───────────────────────┘
```

### 🧱 Componentes Atômicos Padronizados:
* **`CanvasModal`**: Diálogo em 3 camadas (cabeçalho fixo com botão de fechar, corpo com rolagem independente e rodapé de ações ancorado que nunca sobrepõe o conteúdo).
* **`CanvasToggle`**: Switch customizado com thumb branco, trilha no tom de destaque (`colors.accent`) e escala uniforme de `0.85f`.
* **`CanvasButton`**: Botões tipados (`Primary`, `Secondary`, `Outlined`, `Ghost`, `Danger`) com ripple nativo e cantos estruturados.
* **`CanvasTextField`**: Entradas de texto com foco reativo e estados de erro.
* **`CanvasBadge`**: Pílulas de alta legibilidade para estados financeiros e tags com adaptação visual por tema.

---

## 🎬 Splash Screen Animada Cinematográfica

O componente [`SplashScreen.kt`](app/src/main/java/com/canvasstudio/ui/splash/SplashScreen.kt) oferece uma experiência visual de alta fidelidade ao inicializar o aplicativo:

1. **Halo de Luz Ambiente (*Pulse Glow*)**: Brilho radial que pulsa suavemente no centro com `Brush.radialGradient`, criando profundidade espacial na tela OLED.
2. **Grade Reativa em Background**: Revela a matriz de fundo em transição suave (pontos circulares na Apple ou cruzetas técnicas na Teenage Engineering).
3. **Monograma Central com Física de Mola**: O logo surge com dinâmica de mola orgânica (`Spring.DampingRatioMediumBouncy`) envolto por um anel com gradiente angular rotativo.
4. **Expansão Tipográfica (*Letter-Tracking Reveal*)**: O título `CANVAS STUDIO` surge com expansão de espaçamento entre letras de `0.5sp` para `3.5sp`.
5. **Transição de Saída Fluida (*Seamless Exit*)**: Ao carregar, a tela expande suavemente para `1.08x` com fade-out gradual, revelando o Canvas perfeitamente.

---

## 🏛️ Arquitetura Modular por Flows

O sistema adota os princípios de **Clean Architecture** e **Modular Feature-Driven Development**:

```mermaid
graph TD
    subgraph UI_LAYER ["🖥️ PRESENTATION LAYER (Jetpack Compose)"]
        CS["CanvasScreen.kt<br/><i>(Viewport Coordinator & GPU Layer)</i>"]
        SM["SettingsModal.kt<br/><i>(Configurações, GitHub Token & Temas)</i>"]
        DB["DraggableBlock.kt<br/><i>(Atom Host com Gestos Touch)</i>"]
        IN["SidebarContent.kt<br/><i>(Inspetor de Propriedades & Portabilidade)</i>"]
        SP["SplashScreen.kt<br/><i>(Cinematic Animated Launch Engine)</i>"]
    end

    subgraph DESIGN_SYSTEM ["🎨 DESIGN SYSTEM (com.canvasstudio.designsystem)"]
        DS_Tokens["Tokens: Colors, Shapes, Dimens, Typography"]
        DS_Components["Components: Modal, Toggle, Button, TextField, Badge"]
        DS_Theme["CanvasStudioTheme (CompositionLocal Dual-Engine)"]
    end

    subgraph FEATURE_FLOWS ["🧩 FEATURE FLOWS (com.canvasstudio.features)"]
        OCR_FLOW["ocr_importer<br/><i>(SharedMediaImporter & ReceiptAnalyzer)</i>"]
        PORT_FLOW["export_portability<br/><i>(JsonPortabilityService & PdfExporter)</i>"]
        GH_FLOW["github_sync<br/><i>(GitHubApiService & GallerySyncService)</i>"]
    end

    subgraph DOMAIN_CORE ["🧠 DOMAIN & STATE (Unidirectional Data Flow)"]
        VM["BlockViewModel.kt<br/><i>(Reactive State Hub & Coroutine Scope)</i>"]
        CFG["CanvasConfig.kt<br/><i>(Domain Preferences)</i>"]
        RM["ReceiptModels.kt<br/><i>(Domain Models)</i>"]
    end

    subgraph DATA_PERSISTENCE ["💾 DATA LAYER"]
        REPO["BlockRepository.kt<br/><i>(Atomic Sync)</i>"]
        ROOM[("Room SQLite DB")]
        DSTORE[("DataStore Preferences")]
    end

    DESIGN_SYSTEM --> UI_LAYER
    UI_LAYER -->|Events / Intents| VM
    VM -->|StateFlow / Immutable State| UI_LAYER
    VM --> FEATURE_FLOWS
    VM --> REPO
    REPO --> ROOM
    REPO --> DSTORE
```

---

## 🔬 Motores de Engenharia & Performance GPU

```
┌────────────────────────────────────────────────────────────────────────┐
│                        GPU ACCELERATED PIPELINE                        │
│                                                                        │
│   Touch Gestures ──► detectTransformGestures ──► Offset/Scale Mutable   │
│                                                       │                │
│                                                       ▼                │
│                 Draw Phase (Zero Recomposition) ◄── graphicsLayer      │
└────────────────────────────────────────────────────────────────────────┘
```

### 1. Motor de Transformação Espacial ($\mathcal{O}(1)$)
A navegação pelo Canvas não dispara recomposições na árvore de composables. As matrizes de translação e escala são aplicadas na fase de renderização da GPU via `graphicsLayer`:
$$\begin{bmatrix} X_{render} \\ Y_{render} \\ 1 \end{bmatrix} = \begin{bmatrix} S & 0 & T_x \\ 0 & S & T_y \\ 0 & 0 & 1 \end{bmatrix} \begin{bmatrix} X_{world} \\ Y_{world} \\ 1 \end{bmatrix}$$

### 2. Motor de Grade Magnética Infinita (*State Deferral*)
O componente `CanvasBackground` consome o estado de escala e offset através de provedores de função lambda (`scale = { scale }`, `offset = { offset }`). Isso permite que a grade trace seus eixos ortogonais no `DrawScope` sem invalidar os nós estruturais da interface.

### 3. Motor Poligonal Multieixo (Radar Chart)
Renderizador trigonométrico para diagramação radial com eixos uniformes em intervalos de $60^\circ$ ($\frac{\pi}{3}\text{ rad}$):
$$X_i = C_x + \left( \frac{V_i}{V_{max}} \cdot R \right) \cos\left( i \cdot \frac{\pi}{3} - \frac{\pi}{2} \right)$$
$$Y_i = C_y + \left( \frac{V_i}{V_{max}} \cdot R \right) \sin\left( i \cdot \frac{\pi}{3} - \frac{\pi}{2} \right)$$

---

## 🤖 Pipeline de IA & OCR On-Device

O aplicativo integra um pipeline assíncrono para processar imagens e arquivos PDF compartilhados diretamente de apps bancários (Itaú, Nubank, Banco do Brasil, Bradesco, Santander, Inter, etc.):

```mermaid
sequenceDiagram
    autonumber
    participant AppExt as App Externo / Galeria
    participant MainAct as MainActivity (Intent Filter)
    participant Importer as SharedMediaImporter (IO)
    participant MLKit as Google ML Kit Vision
    participant Analyzer as ReceiptAnalyzer (Regex Engine)
    participant RoomDB as Room Database
    participant Canvas as Canvas UI (Compose)

    AppExt->>MainAct: Intent ACTION_SEND (PDF / Imagem)
    MainAct->>Importer: importMedia(uri, autoOcrEnabled)
    alt Arquivo PDF
        Importer->>Importer: Renderiza Página 0 via PdfRenderer (ARGB_8888)
    else Imagem JPG/PNG
        Importer->>Importer: Decodifica Bitmap com downscaling seguro (inSampleSize)
    end
    Importer->>Importer: Salva arquivo em context.filesDir/canvas_images
    Importer->>MLKit: Processa Bitmap com TextRecognizer
    MLKit-->>Importer: Retorna Texto Completo Reconhecido
    Importer->>Analyzer: analyze(rawText, fileName)
    Analyzer->>Analyzer: Extrai Valor, Data, Pagador (De), Recebedor (Para) e PIX
    Analyzer-->>Importer: Retorna AnalyzedReceipt
    Importer->>RoomDB: Insere BlockEntity (com metadados e URI local)
    RoomDB-->>Canvas: Emite novo StateFlow e exibe Bloco instantaneamente com Badges
```

### 🎯 Capacidades do Extrator:
* **Identificação de Transação PIX**: Detecta chaves, termos bancários e aplica badge ciano `[PIX]`.
* **Segregação Rigorosa de Partes**: Separação precisa entre Pagador/Origem (`De`) e Destinatário/Favorecido (`Para`), eliminando falsos positivos com preposições.
* **Formatador Monetário**: Conversão de strings de OCR para floats numéricos auditáveis (`R$ 1.250,50` $\rightarrow$ `1250.50f`).
* **Timestamp**: Captura e formatação de data/hora da realização do pagamento.

---

## 🌐 Automação Cloud & Sync Direto (GitHub REST API)

O módulo [`GitHubApiService.kt`](app/src/main/java/com/canvasstudio/features/export_portability/GitHubApiService.kt) permite o envio e commit direto de imagens do celular para o repositório da sua Galeria Web:

```mermaid
sequenceDiagram
    participant User as 👤 Usuário (App)
    participant UI as 📱 Sidebar (Exportar para Galeria)
    participant Sync as ⚡ GallerySyncService
    participant GH as 🐙 GitHub REST API (Contents API)
    participant Repo as 📁 Repositório SollonSoares/galeria/imagens/
    participant Web as 🌐 GitHub Pages (Web)

    User->>UI: Toca em "Exportar para Galeria"
    UI->>Sync: syncBlocksToGallery(blocks, token)
    Sync->>GH: PUT /repos/SollonSoares/galeria/contents/imagens/{file}.jpg
    GH->>Repo: Commit automático na branch 'main'
    GH-->>Sync: 201 Created (SHA + html_url)
    Sync->>UI: Atualiza blocos para https://sollonsoares.github.io/galeria/imagens/{file}.jpg
    UI-->>User: "🚀 N imagem(ns) enviadas e comitadas no GitHub com sucesso!"
    Repo-->>Web: GitHub Pages serve a imagem em segundos
```

---

## 📄 Portabilidade e Esquema de Dados (v2.0.0)

O Canvas Studio adota portabilidade bidirecional compatível com a Web através do serviço isolado `JsonPortabilityService`.

### 📑 Esquema JSON Exportado (`JSON Export`):
Todos os valores financeiros, metadados e o **texto bruto completo do OCR (`rawText`)** são preservados diretamente dentro do array `campos` de cada bloco:

```json
{
  "metadata": {
    "versao": "2.0.0",
    "timestamp": 1787303429050,
    "brand": "Canvas Studio"
  },
  "blocos": {
    "data_t_1_45231": {
      "top": "60px",
      "left": "60px",
      "width": 320,
      "height": 480,
      "type": "image",
      "title": "Pix - João Silva",
      "url": "https://sollonsoares.github.io/galeria/imagens/comprovante_pix_1.jpg",
      "campos": [
        {
          "html": "<div><b>Valor:</b> R$ 150,00</div><div><b>Realizado em:</b> 21/08/2026 14:30</div><div><b>De (Pagador):</b> SOLLON SOARES</div><div><b>Para (Destinatário):</b> JOAO DA SILVA</div><div><b>Instituição:</b> NU PAGAMENTOS S.A.</div>",
          "className": "sub-campo",
          "valor": 150.00,
          "valorFormatted": "R$ 150,00",
          "realizadoEm": "21/08/2026 14:30",
          "isPix": true,
          "de": "SOLLON SOARES",
          "pagador": "SOLLON SOARES",
          "para": "JOAO DA SILVA",
          "destinatario": "JOAO DA SILVA",
          "instituicao": "NU PAGAMENTOS S.A.",
          "banco": "NU PAGAMENTOS S.A.",
          "rawText": "Comprovante de pagamento Pix\nNome: SOLLON SOARES\nRecebedor: JOAO DA SILVA\nValor: R$ 150,00..."
        }
      ]
    }
  }
}
```

---

## 🚀 Guia de Execução e Engenharia

### 📋 Requisitos de Ambiente:
* **JDK:** Java 17 (Azul Zulu ou OpenJDK recomendado).
* **Android SDK:** API Mínima 26 (Android 8.0 Oreo), API Alvo 34 (Android 14).
* **Gradle:** 8.5+ com Kotlin 2.0.21.

### 🛠️ Comandos de Build e Execução:

```bash
# 1. Compilação e Verificação Estática de Tipos
./gradlew compileDebugKotlin

# 2. Execução dos Testes Unitários de Arquitetura
./gradlew testDebugUnitTest

# 3. Geração do Pacote APK de Debug
./gradlew assembleDebug

# 4. Instalação Direta no Dispositivo/Emulador Conectado
./gradlew installDebug
```

---

<div align="center">

**Canvas Studio Compose** • Arquitetura e Engenharia por **Sollon Soares**  
*Distribuído sob a licença [MIT](LICENSE).*

</div>
