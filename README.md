<![CDATA[<div align="center">

# 💰 APEX MONEY

### *Centro de Mando Financiero Personal*

![Android](https://img.shields.io/badge/Android-26+-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-Sync-3FCF8E?style=for-the-badge&logo=supabase&logoColor=white)
![License](https://img.shields.io/badge/Licencia-Privada-E53935?style=for-the-badge)

---

*Una aplicación Android nativa diseñada como un centro de control financiero personal,*  
*con una estética cinematográfica oscura inspirada en atmósferas de alta concentración.*

</div>

---

## 🎯 Visión

Apex Money no es una app de finanzas genérica. Es un **ecosistema de comando personal** construido desde cero con Kotlin y Jetpack Compose, diseñado para quienes ven la gestión del dinero como un acto de disciplina estratégica, no como una tarea doméstica.

Cada pantalla, cada transición, cada decisión de diseño responde a un único principio:  
**Control absoluto sin ruido visual.**

---

## ✨ Funcionalidades

### 📊 Dashboard — Visión General
- Balance total con animaciones en tiempo real
- Resumen de ingresos y gastos del período
- Indicador de salud financiera calculado algorítmicamente
- Modo Discreto con un toque (oculta montos sensibles)

### 💸 Transacciones — Registro Rápido
- Entrada rápida mediante Bottom Sheet flotante
- Categorización automática por tipo (Ingreso / Gasto)
- Historial completo con búsqueda y filtrado

### 📈 Analíticas — Inteligencia Financiera
- Gráficos de distribución por categoría
- Tendencias de gasto mensual
- Métricas comparativas período a período

### 💰 Bóvedas — Ahorro con Propósito
- Bóvedas de ahorro con metas individuales
- Barra de progreso visual por bóveda
- Depósitos y retiros con trazabilidad completa

### 📋 Presupuestos — Control de Límites
- Presupuestos por categoría con alertas visuales
- Seguimiento de consumo en tiempo real
- Indicadores de alerta cuando te acercas al límite

### 🔄 Pagos Recurrentes — Automatización
- Registro de pagos periódicos (suscripciones, servicios)
- Notificaciones inteligentes de vencimiento
- Calendario de próximos cargos

### ⚙️ Ajustes — Centro de Configuración
- 4 temas visuales cinematográficos
- Modo Discreto global
- Autenticación biométrica
- Exportar / Importar datos (CSV)
- Sincronización manual con Supabase
- Papelera de Reciclaje con purga automática de 30 días
- Borrado seguro de datos

---

## 🏗️ Arquitectura

```
com.sravila.apexmoney/
│
├── core/                          # Infraestructura compartida
│   ├── database/                  # Room: Entities, DAOs, Database
│   ├── datastore/                 # DataStore: Preferencias del usuario
│   ├── network/                   # Supabase: Client, SyncWorker, Models
│   ├── theme/                     # Material3: Colores, Tipografía, Temas
│   ├── ui/                        # Componentes reutilizables (ObsidianCard, etc.)
│   └── utils/                     # Formateo de moneda, Calculador de salud
│
├── features/                      # Módulos de funcionalidad
│   ├── dashboard/                 # Pantalla principal + ViewModel
│   ├── analytics/                 # Gráficos y métricas + ViewModel
│   ├── budgets/                   # Presupuestos + ViewModel
│   ├── vaults/                    # Bóvedas de ahorro + ViewModel
│   ├── recurring/                 # Pagos recurrentes + ViewModel + Worker
│   ├── quickentry/                # Bottom Sheets de entrada rápida
│   └── settings/                  # Configuración + ViewModel
│
├── ui/                            # Capa de presentación global
│   ├── navigation/                # Barra de navegación inferior
│   └── splash/                    # Pantalla de inicio animada
│
└── MainActivity.kt                # Punto de entrada + NavHost
```

**Patrón:** MVVM (Model-View-ViewModel) con flujo unidireccional de datos.  
**Principio:** Offline-First — Room como fuente de verdad, Supabase como respaldo en la nube.

---

## 🔄 Motor de Sincronización

Apex Money implementa un motor de sincronización **Offline-First** con resolución de conflictos:

| Fase | Descripción |
|---|---|
| **Push** | Los cambios locales (nuevos, editados, eliminados) se envían a Supabase |
| **Pull** | Los cambios remotos se descargan y se comparan contra la versión local |
| **Conflicto** | Gana la versión con `updatedAt` más reciente — los cambios offline del usuario nunca se sobreescriben |
| **Purga** | Registros eliminados hace más de 30 días se destruyen permanentemente solo si ya fueron sincronizados |

El motor utiliza `WorkManager` con restricciones de red para ejecutarse periódicamente sin consumir batería innecesariamente.

---

## 🛢️ Base de Datos

### Local (Room)
4 entidades principales con soft-delete y timestamps de auditoría:

| Tabla | Descripción |
|---|---|
| `transactions` | Ingresos y gastos |
| `budgets` | Presupuestos por categoría |
| `savings_vaults` | Bóvedas de ahorro con meta |
| `recurring_payments` | Pagos periódicos |

### Nube (Supabase / PostgreSQL)
Espejo exacto de la estructura local con Row Level Security habilitado.

---

## 🎨 Sistema de Diseño

El sistema visual se construye sobre componentes propios del ecosistema **Command Center**:

| Componente | Uso |
|---|---|
| `ObsidianCard` | Contenedor principal con glassmorphism oscuro |
| `ApexButton` | Botón primario con gradiente y animación |
| `MoneyDisplay` | Renderizado de montos con formato inteligente |
| `ProgressBarModular` | Barra de progreso temática |
| `SegmentedControl` | Selector de períodos |
| `AnimatedSplashScreen` | Pantalla de inicio con transiciones cinemáticas |

### Temas Disponibles

| Tema | Inspiración |
|---|---|
| 🔴 **Blood Sun** | The Batman — negro absoluto con acentos rojos |
| 🔵 **Ocean Depths** | Azul marino profundo para sesiones de concentración |
| ⚫ **Neon Night** | AMOLED puro con acentos neón |
| 🌅 **Soft Day** | Modo claro minimalista para exteriores |

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 |
| Navegación | Navigation Compose |
| Base de datos local | Room (SQLite) |
| Preferencias | DataStore (Proto) |
| Sincronización en la nube | Supabase (PostgreSQL + REST) |
| Tareas en segundo plano | WorkManager |
| Notificaciones | NotificationManager + AlarmManager |
| Seguridad | BiometricPrompt |
| Compilación | Gradle 8.11.1 + KSP |

---

## ⚡ Inicio Rápido

### Prerrequisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Dispositivo o emulador con Android 8.0+ (API 26)

### Configuración

1. **Clonar el repositorio:**
```bash
git clone https://github.com/sr-avila-g/Organizer.git
cd Organizer
```

2. **Configurar credenciales de Supabase:**
```bash
# Crear archivo local.properties en la raíz del proyecto
echo "SUPABASE_URL=https://tu-proyecto.supabase.co" >> local.properties
echo "SUPABASE_ANON_KEY=tu-clave-anonima" >> local.properties
```

3. **Compilar y ejecutar:**
```bash
./gradlew installDebug
```

---

## 📁 Configuración de Supabase

Ejecutar el siguiente SQL en el editor de Supabase para crear las tablas necesarias:

```sql
-- Transacciones
CREATE TABLE transactions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'LOCAL_USER',
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    date TEXT NOT NULL,
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

-- Bóvedas de ahorro
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

-- Pagos recurrentes
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

-- Habilitar Row Level Security
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE budgets ENABLE ROW LEVEL SECURITY;
ALTER TABLE savings_vaults ENABLE ROW LEVEL SECURITY;
ALTER TABLE recurring_payments ENABLE ROW LEVEL SECURITY;

-- Políticas de acceso público (desarrollo)
CREATE POLICY "Allow all" ON transactions FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON budgets FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON savings_vaults FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON recurring_payments FOR ALL USING (true) WITH CHECK (true);
```

---

## 📐 Especificaciones Técnicas

| Propiedad | Valor |
|---|---|
| `applicationId` | `com.sravila.apexmoney` |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 34 (Android 14) |
| `compileSdk` | 34 |
| `versionName` | 1.0 |
| Archivos Kotlin | 32 |
| Módulos de Feature | 7 |
| Módulos Core | 6 |

---

<div align="center">

---

### 🖋️ Sr. Avila

*Ingeniería de Software · Sistemas de Control Personal*

---

> *"El que tiene un porqué para vivir, puede soportar casi cualquier cómo."*  
> — **Friedrich Nietzsche**

---

<sub>Desarrollado con disciplina, propósito y obsesión por el detalle.</sub>  
<sub>© 2026 Sr. Avila. Todos los derechos reservados.</sub>

</div>
]]>
