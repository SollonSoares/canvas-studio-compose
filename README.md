<div align="center">

# 🥷 CANVAS STUDIO COMPOSE — NARUTO RPG
### *High-Fidelity Visual Workspace & Modular Shinobi Engine*

<p align="center">
  <strong>A transposição mobile definitiva do Canvas Studio. Interface <em>macOS-inspired</em> com <em>Glassmorphism</em> nativo, orquestrada por uma arquitetura de <em>Reactive Micro-Kernel</em> e <em>UDF</em>.</strong><br>
  Ambiente de engenharia visual com <em>Snap-to-Grid</em> rigoroso, radar trigonométrico de atributos e persistência atômica.
</p>

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/⚡_Android_Native-0a84ff?style=for-the-badge&logo=android&logoColor=white" alt="Android Native"></a>
  <img src="https://img.shields.io/badge/Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose">
  <img src="https://img.shields.io/badge/Architecture-Modular-blue" alt="Modular">
</p>

---

### 📑 Architecture & Design Index
[1. Design System Tokens](#-design-system--tokens-visuais) • 
[2. Modular Architecture](#-arquitetura-modular-do-sistema) • 
[3. Core Engines](#-motores-e-fundamentos-matemáticos) • 
[4. Module Matrix](#-matriz-de-módulos-sistêmicos) • 
[5. Implementation](#-como-executar-localmente)

</div>

---

## 🎨 Design System & Tokens Visuais

O sistema adota o **macOS Glassmorphism Standard**, garantindo profundidade visual e hierarquia clara através de transparências e blur.

<details>
<summary><b>▶️ Especificações de Tokens e Superfícies</b></summary>

<br>

### 💎 Glassmorphism Standard
Toda superfície elevada no sistema deve seguir o contrato de renderização:
- **Alpha:** `0.82f` (Opacidade de fundo).
- **Background Blur:** `20.dp` (Renderização via `graphicsLayer`).
- **Border:** `1.dp` com Alpha `0.1f` (Cor: `White`).
- **Corner Radius:** `16.dp` (Padrão para cards e diálogos).

### 🎨 Color Palette (Tokens)
| Categoria | Token | Valor Hex | Uso Semântico |
| :--- | :--- | :--- | :--- |
| **Accent** | `Apple Blue` | `#0A84FF` | Ações primárias e indicadores de foco. |
| **Danger** | `Apple Red` | `#FF453A` | Ações destrutivas e estados de erro. |
| **Surface** | `Glass Deep` | `#1A1D29` | Base para componentes com blur. |

```kotlin
// Referência de Implementação do Sistema
Modifier
    .clip(RoundedCornerShape(16.dp))
    .background(Color(0x1A1D29).copy(alpha = 0.82f))
    .blur(20.dp)
    .border(1.dp, Color.White.copy(alpha = 0.1f))
```

</details>

---

## 🏛️ Arquitetura Modular do Sistema

O Canvas Studio Compose é dividido em camadas de responsabilidade única, evitando itens avulsos e garantindo escalabilidade via Unidirectional Data Flow (UDF).

<details>
<summary><b>▶️ Visualizar Hierarquia Modular e Fluxo de Dados</b></summary>

<br>

### Diagrama de Micro-Kernel Reativo
```mermaid
graph TB
    subgraph UI_Surface ["🖥️ UI SURFACE (Atoms & Organisms)"]
        Sidebar["SidebarContent.kt<br/><i>(FILTRO • CRIAR • ORGANIZAÇÃO • PORTABILIDADE)</i>"]
        Canvas["BlockScreen.kt<br/><i>(Viewport Coordinator)</i>"]
        Blocks["DraggableBlock.kt<br/><i>(Modular Atom)</i>"]
    end

    subgraph SYSTEM_ENGINE ["⚡ SYSTEM ENGINE (Core Logic)"]
        Grid["CanvasBackground.kt<br/><i>(Snap Engine)</i>"]
        Trig["ChartBlock.kt<br/><i>(Trig Engine)</i>"]
    end

    subgraph KERNEL ["🧠 REACTIVE KERNEL (State)"]
        VM["BlockViewModel.kt<br/><i>(UDF Hub)</i>"]
        Repo["BlockRepository.kt<br/><i>(Atomic Sync)</i>"]
    end

    subgraph PERSISTENCE ["💾 DATA LAYER"]
        Room[("Room DB")]
        DS[("DataStore")]
    end

    VM -->|StateFlow| UI_Surface
    UI_Surface -->|Events| VM
    VM -->|Sync| Repo
    Repo -->|IO| Room
```

</details>

---

## 🔬 Motores e Fundamentos Matemáticos

O sistema opera sob leis matemáticas estritas para garantir paridade 1:1 com a lógica Web e as métricas do RPG.

<details>
<summary><b>▶️ Especificações dos Motores de Cálculo</b></summary>

<br>

### 1. Motor de Snap-to-Grid Magnético (20dp)
O sistema ignora posições intermediárias, forçando o alinhamento magnético atômico:
$$P_{snap} = \text{round}\left( \frac{P_{raw}}{20} \right) \cdot 20$$

### 2. Motor de Radar Trigonométrico (Naruto RPG)
O gráfico de atributos utiliza 6 eixos simétricos com intervalos constantes de $60^\circ$ ($\frac{\pi}{3}$ rad):
- **Ângulo do Eixo $i$:** $\theta_i = (i \cdot \frac{\pi}{3}) - \frac{\pi}{2}$
- **Projeção de Vértice:** 
  - $X = C_x + \left( \frac{V_i}{V_{max}} \cdot R \right) \cdot \cos(\theta_i)$
  - $Y = C_y + \left( \frac{V_i}{V_{max}} \cdot R \right) \cdot \sin(\theta_i)$

</details>

---

## 🗂️ Matriz de Módulos Sistêmicos

Hierarquia modular organizada pela funcionalidade no ecossistema do Design System.

<details>
<summary><b>▶️ Catálogo de Módulos e Responsabilidades</b></summary>

<br>

| Camada | Módulo | Responsabilidade Técnica |
| :--- | :--- | :--- |
| **Viewport** | `Coordenador` | Gestão de escala (0.15f a 3.0f) e translação do palco. |
| **Grid Engine** | `CanvasBackground` | Renderização e cálculo do grid magnético de 20dp. |
| **Dashboard** | `SidebarContent` | Navegação estruturada em: **FILTRO**, **CRIAR**, **ORGANIZAÇÃO**, **PORTABILIDADE**. |
| **Property Editor**| `EditBlockDialog` | Refatoração de propriedades e edição atômica de blocos. |
| **Rich Text** | `RichTextHandler` | Parser de tags Web (html/campos) para `AnnotatedString`. |
| **Chart System** | `ChartBlock` | Motor de radar poligonal simétrico ($\pi/3$ rad). |
| **Image Core** | `Coil integration` | Gerenciamento de cache Lru e renderização assíncrona. |

</details>

---

## 📊 Métricas e Performance

<details>
<summary><b>▶️ Análise de Complexidade e Eficiência</b></summary>

<br>

- **Snap-to-Grid:** $\mathcal{O}(1)$ - Cálculo matemático puro sem iteração.
- **Radar Rendering:** $\mathcal{O}(6)$ - Vértices fixos processados via GPU (DrawScope).
- **State Update:** $\mathcal{O}(N)$ - Sincronização atômica de $N$ blocos via Room Flowbus.

</details>

---

## 🚀 Como Executar Localmente

<details>
<summary><b>▶️ Requisitos de Ambiente de Engenharia</b></summary>

<br>

1. **Android Studio Jellyfish**+ (ou superior).
2. **Gradle 8.5**+ com suporte a Kotlin 2.0.
3. **Dispositivo/Emulador:** Mínimo Android API 26 (Oreo).
4. **Build:** `gradle assembleDebug` para compilação nativa.

</details>

---

<div align="center">

**Sollon Soares** — *Lead Shinobi Engineer*
Distributed under **MIT License**.

</div>
