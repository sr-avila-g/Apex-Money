<div align="center">

<br>

<img src="https://img.shields.io/badge/%E2%96%B2-APEX-000000?style=for-the-badge&labelColor=000000" alt="Apex">

# ◈ APEX MONEY ◈

**`C E N T R O · D E · M A N D O · F I N A N C I E R O`**

<br>

[![Android](https://img.shields.io/badge/Android_8.0+-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin_2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-3FCF8E?style=flat-square&logo=supabase&logoColor=white)](https://supabase.com)
[![Room](https://img.shields.io/badge/Room_DB-FF6F00?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/Privado-E53935?style=flat-square)](/)

<br>

*Una aplicación Android nativa diseñada como un sistema operativo financiero personal,*
*con una estética cinematográfica oscura inspirada en atmósferas de alta concentración.*

<br>

---

</div>

<br>

## ◈ Visión

> Apex Money no es una app de finanzas genérica.
> Es un **ecosistema de comando personal** construido desde cero con Kotlin y Jetpack Compose,
> diseñado para quienes ven la gestión del dinero como un acto de disciplina estratégica.

Cada pantalla, cada transición, cada decisión de diseño responde a un único principio:

```
C O N T R O L   A B S O L U T O   S I N   R U I D O   V I S U A L
```

<br>

---

<br>

## ◈ Funcionalidades

<table>
<tr>
<td width="50%">

### 📊 Dashboard
> Visión general en tiempo real

- Balance animado con salud financiera algorítmica
- Resumen de ingresos y gastos del período
- Modo Discreto con un toque *(oculta montos)*

</td>
<td width="50%">

### 💸 Transacciones
> Registro rápido e inteligente

- Entrada mediante Bottom Sheet flotante
- Categorización por tipo (Ingreso / Gasto)
- Historial completo con búsqueda

</td>
</tr>
<tr>
<td>

### 📈 Analíticas
> Inteligencia financiera visual

- Gráficos de distribución por categoría
- Tendencias de gasto mensual
- Comparativas período a período

</td>
<td>

### 💰 Bóvedas
> Ahorro con propósito

- Bóvedas con metas individuales
- Barra de progreso visual por bóveda
- Depósitos y retiros con trazabilidad

</td>
</tr>
<tr>
<td>

### 📋 Presupuestos
> Control de límites

- Presupuestos por categoría con alertas
- Seguimiento de consumo en tiempo real
- Indicadores al acercarte al límite

</td>
<td>

### 🔄 Recurrentes
> Automatización de pagos

- Registro de suscripciones y servicios
- Notificaciones de vencimiento
- Calendario de próximos cargos

</td>
</tr>
<tr>
<td>

### 💳 Multi-Cuentas
> Flexibilidad financiera total

- Gestión de Bancos, Billeteras y Efectivo
- Soporte para Tarjetas de Crédito con cálculo de deuda
- Transferencias entre cuentas con balance automático

</td>
<td>

### ☁️ Sincronización BYOB
> Trae Tu Propia Nube (Local-first)

- Supabase PostgreSQL como respaldo silencioso
- Modo offline garantizado por Room
- Resolución de conflictos priorizando lo más reciente

</td>
</tr>
</table>

<br>

### ⚙️ Centro de Configuración

<div align="center">

`4 Temas Cinematográficos` · `Modo Discreto` · `Biometría` · `Export/Import CSV` · `Sync Manual` · `Papelera 30 días` · `Borrado Seguro`

</div>

<br>

---

<br>

## ◈ Arquitectura

```
com.sravila.apexmoney/
│
├─ core/                             ── Infraestructura ──
│   ├─ database/                     Room: Entities, DAOs, Database
│   ├─ datastore/                    DataStore: Preferencias
│   ├─ network/                      Supabase: Client, SyncWorker, Models
│   ├─ theme/                        Material3: Colores, Tipografía, Temas
│   ├─ ui/                           Componentes (ObsidianCard, ApexButton...)
│   └─ utils/                        Formateo, Calculador de Salud
│
├─ features/                         ── Módulos ──
│   ├─ dashboard/                    Pantalla principal + ViewModel
│   ├─ analytics/                    Gráficos y métricas + ViewModel
│   ├─ accounts/                     Gestión de Cuentas, Bancos y TC + ViewModel
│   ├─ budgets/                      Presupuestos + ViewModel
│   ├─ vaults/                       Bóvedas de ahorro + ViewModel
│   ├─ recurring/                    Pagos recurrentes + Worker
│   ├─ quickentry/                   Bottom Sheets de entrada
│   └─ settings/                     Configuración + ViewModel
│
├─ ui/                               ── Presentación ──
│   ├─ navigation/                   Barra inferior
│   └─ splash/                       Pantalla animada
│
└─ MainActivity.kt                   Punto de entrada + NavHost
```

<div align="center">

**Patrón** · MVVM con flujo unidireccional &nbsp;&nbsp;│&nbsp;&nbsp; **Principio** · Offline-First (Room → Supabase)

</div>

<br>

---

<br>

## ◈ Motor de Sincronización

<div align="center">

```
╔══════════╗     ╔══════════╗     ╔══════════════╗     ╔══════════╗
║   PUSH   ║ ──▶ ║   PULL   ║ ──▶ ║  CONFLICTO   ║ ──▶ ║  PURGA   ║
║ Local→☁️  ║     ║ ☁️→Local  ║     ║ Gana +nuevo  ║     ║ 30 días  ║
╚══════════╝     ╚══════════╝     ╚══════════════╝     ╚══════════╝
```

</div>

| Fase | Descripción |
|:---:|---|
| **Push** | Cambios locales (nuevos, editados, eliminados) → Supabase |
| **Pull** | Cambios remotos se descargan y comparan contra versión local |
| **Conflicto** | Gana `updatedAt` más reciente — cambios offline nunca se sobreescriben |
| **Purga** | Registros +30 días se destruyen **solo** si ya fueron sincronizados |

> Motor ejecutado vía `WorkManager` con restricciones de red. Cero consumo de batería sin conexión.

<br>

---

<br>

## ◈ Sistema de Diseño

<table>
<tr>
<td align="center" width="25%">
<h4>🔴 Blood Sun</h4>
<sub>The Batman</sub><br>
<sub>Negro absoluto · Acentos rojos</sub>
</td>
<td align="center" width="25%">
<h4>🔵 Ocean Depths</h4>
<sub>Azul Marino</sub><br>
<sub>Profundidad · Concentración</sub>
</td>
<td align="center" width="25%">
<h4>⚫ Neon Night</h4>
<sub>AMOLED Puro</sub><br>
<sub>Negro total · Acentos neón</sub>
</td>
<td align="center" width="25%">
<h4>🌅 Soft Day</h4>
<sub>Minimalista</sub><br>
<sub>Claro · Exteriores</sub>
</td>
</tr>
</table>

<br>

### Componentes del Ecosistema Command Center

```
ObsidianCard           →  Contenedor con glassmorphism oscuro
ApexButton             →  Botón primario con gradiente animado
MoneyDisplay           →  Renderizado inteligente de montos
ProgressBarModular     →  Barra de progreso temática
SegmentedControl       →  Selector de períodos
AnimatedSplashScreen   →  Transiciones cinemáticas de entrada
```

<br>

---

<br>

## ◈ Stack Tecnológico

```
┌──────────────────┬──────────────────────────────────────┐
│  Lenguaje        │  Kotlin 2.0                          │
│  UI              │  Jetpack Compose + Material Design 3 │
│  Navegación      │  Navigation Compose                  │
│  DB Local        │  Room (SQLite)                       │
│  Preferencias    │  DataStore                           │
│  Nube            │  Supabase (PostgreSQL + REST)        │
│  Background      │  WorkManager                         │
│  Notificaciones  │  NotificationManager + AlarmManager  │
│  Seguridad       │  BiometricPrompt                     │
│  Build           │  Gradle 8.11.1 + KSP                │
└──────────────────┴──────────────────────────────────────┘
```

<br>

---

<br>

## ◈ Inicio Rápido

### Prerrequisitos
- Android Studio Hedgehog (2023.1.1)+
- JDK 17
- Android 8.0+ (API 26)

### 1 · Clonar
```bash
git clone https://github.com/sr-avila-g/Apex-Money.git
cd Apex-Money
```

### 2 · Configurar Supabase (Trae tu propio Backend)

> **Opcional:** Apex Money es *Local-First*. Tus datos nunca salen del dispositivo a menos que tú lo decidas. 
> Si deseas sincronización en la nube, puedes levantar tu propia instancia gratuita en [Supabase](https://supabase.com).
> 
> 1. Crea un proyecto en Supabase.
> 2. Ejecuta el script SQL (ver abajo) en el SQL Editor.
> 3. En la app (dentro de tu celular), ve a **Ajustes > Nube y Sincronización > Configurar Backend Propio**.
> 4. Pega tu `SUPABASE_URL` y tu `ANON_KEY`.

*(Si eres desarrollador y compilas tu propio APK, puedes quemar las variables en `local.properties`):*
```bash
echo "SUPABASE_URL=https://tu-proyecto.supabase.co" >> local.properties
echo "SUPABASE_ANON_KEY=tu-clave-anonima" >> local.properties
```

### 3 · Compilar
```bash
./gradlew installDebug
```

### 4 · Instalar directamente
> Descarga el APK desde la sección de [**Releases**](../../releases) de este repositorio.

<br>

---

<br>

<details>
<summary><strong>◈ Configuración de Supabase (SQL)</strong></summary>

<br>

Ejecutar en el SQL Editor de Supabase:

```sql
-- ═══════════════════════════════════════════════
-- APEX MONEY · Esquema de Base de Datos
-- ═══════════════════════════════════════════════

-- Cuentas (Billeteras / Bancos)
CREATE TABLE accounts (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'LOCAL_USER',
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    initial_balance REAL NOT NULL DEFAULT 0,
    color_hex TEXT NOT NULL,
    icon_name TEXT NOT NULL,
    is_credit_card BOOLEAN NOT NULL DEFAULT false,
    updated_at TEXT NOT NULL DEFAULT now()::text,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Transacciones
CREATE TABLE transactions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'LOCAL_USER',
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    date TEXT NOT NULL,
    account_id TEXT NOT NULL,
    destination_account_id TEXT,
    is_recurring BOOLEAN NOT NULL DEFAULT false,
    updated_at TEXT NOT NULL DEFAULT now()::text,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Presupuestos
CREATE TABLE budgets (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'LOCAL_USER',
    category TEXT NOT NULL,
    monthly_limit REAL NOT NULL,
    current_spent REAL NOT NULL DEFAULT 0,
    period TEXT NOT NULL,
    updated_at TEXT NOT NULL DEFAULT now()::text,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Bóvedas de Ahorro
CREATE TABLE savings_vaults (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'LOCAL_USER',
    name TEXT NOT NULL,
    target_amount REAL NOT NULL,
    current_amount REAL NOT NULL DEFAULT 0,
    icon TEXT,
    color TEXT,
    updated_at TEXT NOT NULL DEFAULT now()::text,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Pagos Recurrentes
CREATE TABLE recurring_payments (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'LOCAL_USER',
    name TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    frequency TEXT NOT NULL,
    next_payment_date TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TEXT NOT NULL DEFAULT now()::text,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Row Level Security
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE budgets ENABLE ROW LEVEL SECURITY;
ALTER TABLE savings_vaults ENABLE ROW LEVEL SECURITY;
ALTER TABLE recurring_payments ENABLE ROW LEVEL SECURITY;

-- Políticas (desarrollo)
CREATE POLICY "Allow all" ON accounts FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON transactions FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON budgets FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON savings_vaults FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON recurring_payments FOR ALL USING (true) WITH CHECK (true);
```

</details>

<br>

---

<br>

## ◈ Especificaciones

<div align="center">

| | |
|:---:|:---:|
| `applicationId` | `com.sravila.apexmoney` |
| `minSdk` | 26 · Android 8.0 |
| `targetSdk` | 34 · Android 14 |
| `versionName` | 2.0 |
| Archivos Kotlin | 32 |
| Features | 7 módulos |
| Core | 6 módulos |

</div>

<br>

---

<br>

<div align="center">

<br>

```
═══════════════════════════════════════════════════════
```

<br>

### ◈ Sr. Avila

**Ingeniería de Software · Sistemas de Control Personal**

<br>

> *"El que tiene un porqué para vivir, puede soportar casi cualquier cómo."*
>
> — **Friedrich Nietzsche**

<br>

```
═══════════════════════════════════════════════════════
```

<br>

<sub>Desarrollado con disciplina, propósito y obsesión por el detalle.</sub>

<sub>© 2026 Sr. Avila · Todos los derechos reservados.</sub>

<br>

</div>
