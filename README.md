# PaperCPU - MOV-Only Architecture Simulator

[Try it!](https://semoro.github.io/paper-computer)

PaperCPU is an interactive simulator for a minimal "paper computer" architecture where every instruction is a single `MOV` operation encoded as a four-digit decimal number. This educational tool demonstrates fundamental computer architecture concepts through a simplified model.

## Features

- **MOV-Only Architecture**: Experience a computer that operates using only data movement instructions
- **Visual Execution**: Watch data flow between memory cells with animated arrows
- **Step-by-Step Debugging**: Execute one instruction at a time or run automatically
- **Step-Back Capability**: Rewind execution up to 100 steps
- **Memory Visualization**: View all memory cells in a grid with read/write indicators
- **Program Editor**: Create and modify programs in the ROM area
- **I/O Controls**: Input values and see output history
- **Cross-Platform**: Runs on Desktop, Web, Android, and iOS

## Architecture Specification

### Core Model

| Item | Details |
| ---- | ------- |
| **Word size** | 4-digit unsigned decimal (0 – 9999) |
| **Memory size** | 100 cells, addresses `00` – `99` |
| **Instruction format** | `SSDD` where `SS` = source address, `DD` = destination address |
| **Execution rule** | `memory[DD] ← memory[SS]`, then `PC ← PC + 1` |
| **HALT** | Execution stops when the current instruction value is `0000` |

### Memory Layout

| Address   | Name / Purpose |
|-----------| -------------- |
| `00`      | **HALT opcode** (always contains `0000`) |
| `01`      | **PC** – Program Counter |
| `02`      | **A** – Operand A |
| `03`      | **B** – Operand B |
| `04`      | **A + B** (computed automatically) |
| `05`      | **A − B** (computed automatically) |
| `06`      | **A > B** (1 / 0) (computed automatically) |
| `07`      | **A if C else B** (computed automatically) |
| `08`      | **C** – Operand C |
| `09`      | **TMP** – Scratch register |
| `10`      | **GP₁** – General purpose |
| `11`      | **OUT** – Output register |
| `12`      | **IN** – Input register |
| `20`–`49` | **RAM** – General data storage |
| `50`–`99` | **ROM** – Program area |

## Getting Started

### Prerequisites

- JDK 11 or higher
- Gradle 7.0 or higher

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/semoro/PaperCPU.git
   cd PaperCPU
   ```

2. Build the project:
   ```
   ./gradlew build
   ```

### Running the Application

#### Desktop

```
./gradlew :composeApp:run
```

#### Web

```
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

#### Android

```
./gradlew :composeApp:installDebug
```

## Usage Guide

1. **Program Creation**: Edit cells in the ROM area (addresses 50-99) to create your program
2. **Execution Control**:
  - Use the "Step" button to execute one instruction
  - Use "Run/Pause" to execute continuously
  - Use "Step Back" to revert to the previous state
  - Use "Reset" to clear memory and start over
3. **Input/Output**: Enter values in the input field to store in the IN register (address 12), and watch the output log for values written to the OUT register (address 11)

### Sample Program

The default program takes input values for A and B, compares them, and outputs either sum or subtraction of it:

```
50: 1202 - MOV IN to A (Copy input (addr 12) to A (addr 02))
51: 1203 - MOV IN to B (Copy input (addr 12) to B (addr 03))
52: 0608 - MOV CMP to C (Copy comparison result (addr 06) to C (addr 08))
53: 0409 - MOV SUM to TMP (Copy sum (addr 04) to TMP (addr 09))
54: 0502 - MOV SUB to A (Copy subtraction result (addr 05) to A (addr 02))
55: 0903 - MOV TMP to B (Copy TMP (addr 09) to B (addr 03))
56: 0711 - MOV TRN to OUT (Copy ternary result (addr 07) to OUT (addr 11))
57: 0000 - HALT
```

## Technologies Used

- **Kotlin Multiplatform**: For cross-platform code sharing
- **Compose Multiplatform**: For UI across all platforms
- **Kotlin Coroutines**: For reactive programming and background processing
- **Kotlin/Wasm**: For web deployment

## Project Structure

- `/composeApp`: Contains the shared Compose Multiplatform code
  - `commonMain`: Code shared across all platforms
  - Platform-specific folders for platform-specific implementations
- `/iosApp`: iOS application entry point
- `/.github/workflows`: GitHub Actions workflows
  - `deploy-wasm.yml`: Workflow to deploy the WASM build to GitHub Pages

## License

[MIT License](LICENSE)

## Acknowledgements

This project was inspired by educational "paper computers" used to teach fundamental computing concepts.
