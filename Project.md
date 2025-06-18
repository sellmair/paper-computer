### MOV-Only Architecture Simulator – **Comprehensive Specification**

*Target platform: **Compose Multiplatform (Desktop/JVM)***

---

## 1 · Purpose & Scope

Build an interactive desktop application that **demonstrates a minimal “paper computer”** in which **every instruction is a single `MOV`** encoded as a four-digit decimal number.
Learners can **step forward, step back, or run automatically**, view registers and memory, and observe data flow in real time.

---

## 2 · Core Model

| Item                   | Details                                                      |
| ---------------------- | ------------------------------------------------------------ |
| **Word size**          | 4-digit unsigned decimal (0 – 9999)                          |
| **Memory size**        | 100 cells, addresses `00` – `99`                             |
| **Instruction format** | `SSDD` `SS` = source address, `DD` = destination address     |
| **Execution rule**     | `memory[DD] ← memory[SS]`, then `PC ← PC + 1`                |
| **HALT**               | Execution stops when the current instruction value is `0000` |

---

## 3 · Fixed Memory Layout

| Addr      | Name / Meaning           | Notes (read-only = 🔒)                                |
| --------- | ------------------------ | ----------------------------------------------------- |
| `00`      | **HALT opcode** (`0000`) | Always contains `0000` 🔒                             |
| `01`      | **PC** – Program Counter | Holds address of next instruction                     |
| `02`      | **A** – Operand A        | User writable                                         |
| `03`      | **B** – Operand B        | “                                                     |
| `04`      | **A + B**                | Updated automatically after every step                |
| `05`      | **A − B**                | “                                                     |
| `06`      | **A > B** (1 / 0)        | “                                                     |
| `07`      | **A if C else B**        | “                                                     |
| `08`      | **C** – Operand C        | User writable                                         |
| `09`      | **TMP** – Scratch        | General purpose                                       |
| `10`      | **GP₁**                  | General purpose                                       |
| `11`      | **OUT** – Output         | UI log appends when value changes                     |
| `12`      | **IN** – Input           | UI writes into this cell                              |
| `30`–`49` | **RAM**                  | General data storage                                  |
| `50`–`99` | **ROM**                  | Program area; edited before run, locked during run 🔒 |

---

## 4 · Execution Cycle

```kotlin
while (true) {
    val pc = memory[1]
    val instr = memory[pc]
    if (instr == 0) break          // HALT

    val src = instr / 100          // high two digits
    val dst = instr % 100          // low two digits

    pushHistory(pc, src, dst)      //  ⇢ section 5
    memory[dst] = memory[src]
    recomputeDerivedRegisters()    // cells 04-07
    memory[1] = (pc + 1) % 100
}
```

---

## 5 · Step-Back Debugging

| Requirement      | Implementation                                                                         |
| ---------------- | -------------------------------------------------------------------------------------- |
| **Depth**        | Up to **100** most-recent actions                                                      |
| **Storage**      | Ring buffer of structs: `{ pcBefore, instr, src, dst, valueSrc, valueDst, outBefore }` |
| **Reversal**     | Pop last entry, restore `PC`, `memory[src]`, `memory[dst]`, output log                 |
| **Invalidation** | Clearing the buffer on **Reset**, or when the user manually edits memory               |

---

## 6 · Software Architecture

### 6.1 Modules

| Module                     | Responsibilities                                                          |
| -------------------------- | ------------------------------------------------------------------------- |
| **core-sim** (pure Kotlin) | Memory array, execution loop, derived-register calculator, history buffer |
| **ui-desktop** (Compose)   | Rendering, user interaction, file IO                                      |
| **view-model**             | `MutableStateFlow`/`StateFlow` bridging **core-sim** and **ui-desktop**   |

### 6.2 Threading

* **Simulation** runs on a background coroutine to keep UI responsive.
* UI subscribes to state flows; recomposition is automatic.

---

## 7 · UI Specification

### 7.1 Layout Grid (four blocks)

| Block              | Key widgets                                                                                                                                                                                        | Behaviour                                                                |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| **Registers**      | Vertical list of labeled values (`PC`, `A`, `B`, `C`, derived results)                                                                                                                             | Auto-updates every step; derived cells rendered in italic or other style |
| **Memory**         | 10 × 10 `LazyVerticalGrid`. Each cell shows:<br>• address<br>• value<br>• **read arrow (→)** if `addr == SS`<br>• **write arrow (←)** if `addr == DD`<br>• **PC highlight** if `addr == memory[1]` | Cells 50–99 locked during run; hover shows tool-tips                     |
| **Program ROM**    | Scrollable list/editor of addresses `50‒99` with:<br>• 4-digit opcode field<br>• live decode preview (“32 → 42”)                                                                                   | Disabled during run; supports load/save from text/JSON                   |
| **I/O + Controls** | - `TextField` to inject value → `memory[12]`<br>- `LazyColumn` showing OUT history (`memory[11]`)<br>- Buttons: **Step**, **Run/Pause**, **Step Back**, **Reset**, (**Load**, **Save** optional)   | Button enablement reflects sim state and history depth                   |

> Compose layout skeleton
>
> ```kotlin
> Column {
>     Row { Registers(); MemoryGrid() }
>     Row { ProgramPanel(); IoAndControls() }
> }
> ```

### 7.2 Visual Language

* **Read arrow**: thin green arrow (🠖) overlay on right edge.
* **Write arrow**: thin red arrow (🠔) overlay on left edge.
* **PC highlight**: 2 dp blue border + light blue background pulse.
* Use Material 3 theming; expose light/dark toggle.

---

## 8 · File Format

```jsonc
{
  "memory": [0, 50, 1234, …, 0],    // 100 ints
  "breakpoints": [/* future use */]
}
```

Saved with `.movsim` extension.

---

## 9 · Performance & Limits

* Simulation tick ≥ 10 k steps/s on mid-tier desktop.
* UI recomposition bounded to 30 fps when running.
* History buffer fixed at 100 entries (configurable in future).

---

## 10 · Roadmap

| Phase | Milestone                                                 |
| ----- | --------------------------------------------------------- |
| **0** | Project setup, Gradle, Compose Desktop skeleton           |
| **1** | Core simulator with Step / Run / Reset                    |
| **2** | Registers + Memory grid (read/write arrows, PC highlight) |
| **3** | Program ROM editor & file IO                              |
| **4** | Step-back history & UI wiring                             |
| **5** | I/O panel, output logging                                 |
| **6** | Polish (themes, animations, error handling) & packaging   |

---

## 11 · Future Extensions (non-essential)

* **Breakpoints & watchpoints**
* **Macro assembler** with labels → auto-encode `MOV` opcodes
* **Automated test harness**: run program headless and verify OUT sequence
* **Web target** (Compose WASM) once Compose 1.6+ stabilises

---

This specification consolidates **all functional, visual, and architectural decisions** discussed so far and is ready to hand off for implementation or further review.
