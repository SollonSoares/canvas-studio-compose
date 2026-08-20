<div align="center">

# 🥷 CANVAS STUDIO COMPOSE — NARUTO RPG
### *Next-Gen Android Visual Workspace & Shinobi Sheet Engine*

<p align="center">
  <strong>A transposição mobile definitiva do Canvas Studio, construída com Jetpack Compose.</strong><br>
  Um ambiente de orquestração de fichas shinobi com snap-to-grid de 20dp, radar trigonométrico de atributos e arquitetura reativa moderna para Android.
</p>

---

<!-- BADGES TECH STACK -->
<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose">
  <img src="https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3">
  <img src="https://img.shields.io/badge/Room_DB-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Room">
  <img src="https://img.shields.io/badge/MVVM_Architecture-00C853?style=for-the-badge" alt="MVVM">
  <img src="https://img.shields.io/badge/License-MIT-purple?style=for-the-badge" alt="MIT License">
</p>

<!-- QUICK NAVIGATION -->
<p align="center">
  <a href="#-visão-geral--filosofia">Visão Geral</a> •
  <a href="#-arquitetura-do-projeto">Arquitetura</a> •
  <a href="#-fundamentos-matemáticos">Matemática & Motores</a> •
  <a href="#-design-system--tokens">Design System</a> •
  <a href="#-stack-tecnológica">Stack Tecnológica</a> •
  <a href="#-como-executar">Como Executar</a>
</p>

</div>

---

## 🧭 Visão Geral & Filosofia

O **Canvas Studio Compose** é a evolução mobile da aplicação Web original. Ele mantém a promessa de ser uma ferramenta **Local-First**, garantindo que mestres e jogadores tenham total soberania sobre seus dados, com latência zero e funcionamento 100% offline.

A interface Android foi meticulosamente desenhada para replicar a experiência *Glassmorphic* do macOS, adaptando os gestos de arrastar e redimensionar para o paradigma de toque do Android, mantendo a precisão matemática do grid de 20dp.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                         CANVAS STUDIO COMPOSE ENGINE                             │
├──────────────────────┬─────────────────────────────┬─────────────────────────────┤
│  🎨 COMPOSE CANVAS    │  🧠 MVVM + COROUTINES       │  💾 PERSISTÊNCIA NATIVA     │
│  - Snap-to-Grid (20dp)│  - StateFlow-Driven UI      │  - Room (Blocos & Conteúdo) │
│  - Zoom & Pan (0.15x) │  - Hilt/Manual DI           │  - DataStore (Preferências) │
│  - Radar Trigonométrico│  - Kotlin Serialization     │  - Coil (Media Caching)     │
└──────────────────────┴─────────────────────────────┴─────────────────────────────┘
```

---

## 🏛️ Arquitetura do Projeto

Seguindo os princípios de **Clean Architecture** e **UDF (Unidirectional Data Flow)**, o projeto é estruturado para garantir desacoplamento total entre a lógica de negócio e a renderização.

### Estrutura de Pacotes:
*   **`ui.block`**: Orquestração da tela principal (Canvas).
    *   `components/`: Blocos atomizados (`DraggableBlock`, `ImageBlock`, `CanvasBackground`).
    *   `dialogs/`: Modais de edição e configurações.
    *   `utils/`: Utilitários de parsing de Rich Text e lógica de UI.
*   **`data`**: Camada de persistência.
    *   `local/entity/`: Definições de tabela do Room (Schemas Web-compatíveis).
    *   `local/dao/`: Interfaces de acesso ao banco de dados.
    *   `preferences/`: Gerenciamento de estado global (Grade, Dark Mode) via DataStore.
*   **`viewmodel`**: Gestão de estado reativo usando `StateFlow` e `collectAsStateWithLifecycle`.

---

## 🔬 Fundamentos Matemáticos

O motor do Canvas Studio Compose utiliza algoritmos de precisão para espelhar o comportamento da versão Web:

### 1. Motor de Projeção Polar (`ChartComponent`)
Renderização de radar de atributos shinobi baseada em $\theta_i = (i \cdot \frac{\pi}{3}) - \frac{\pi}{2}$.
A normalização segue a regra oficial de RPG Naruto, com teto de 8.0 e arredondamento em passos de 0.5.

### 2. Snap-to-Grid Magnético
Lógica de arredondamento baseada em densidade de pixels (DP):
$$X_{snap} = \text{round}(X / 20) \cdot 20$$
Isso garante que cada movimento de bloco no Android se alinhe perfeitamente aos layouts criados na Web.

### 3. Zoom & Transformação Espacial
Implementação nativa usando `Modifier.transformable` e `Modifier.pointerInput`, permitindo navegação fluida em telas de qualquer tamanho, do smartphone ao tablet.

---

## 🎨 Design System & Tokens

Fiel ao estilo **macOS Glassmorphism**, o app utiliza tokens de cores e transparências específicos:

| Token | Valor Hex/Alpha | Descrição |
| :--- | :--- | :--- |
| **Accent Blue** | `#0A84FF` | Azul vibrante padrão Apple |
| **Danger Red** | `#FF453A` | Vermelho de sistema Apple |
| **Glass Alpha** | `0.82` (D1) | Opacidade dos cards para efeito de desfoque |
| **Grid Step** | `20.dp` | Unidade fundamental de alinhamento |

---

## 🛠️ Stack Tecnológica

*   **Linguagem:** Kotlin + Coroutines (Assincronia)
*   **UI:** Jetpack Compose (Modern Toolkit)
*   **Imagens:** Coil (Carregamento e Cache otimizado)
*   **Banco de Dados:** Room (SQLite com abstração segura)
*   **Configurações:** DataStore Preferences
*   **JSON:** Kotlinx Serialization (Compatibilidade total com o schema Web)

---

## 🚀 Como Executar

1. Certifique-se de ter o **Android Studio Iguana** (ou superior) instalado.
2. Clone o repositório:
   ```bash
   git clone https://github.com/sollonsoares/canvas-studio-compose.git
   ```
3. Abra o projeto no Android Studio.
4. Sincronize o Gradle.
5. Execute em um emulador ou dispositivo físico (API 26+).

---

<div align="center">

### 👨‍💻 Autor & Mantenedor

**Sollon Soares**  
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat-square&logo=github&logoColor=white)](https://github.com/sollonsoares)

<br>

<sub>Este projeto é a implementação Android oficial do ecossistema Canvas Studio.</sub>

</div>
