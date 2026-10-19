# Huerto Escolar 🌱

## Descripción del proyecto

**Huerto Escolar** es una aplicación móvil para Android desarrollada como proyecto académico de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**. Su propósito es facilitar la organización de actividades de un huerto escolar, permitiendo registrar sectores y cultivos, gestionar tareas y revisar las evidencias de los trabajos realizados.

La aplicación funciona con datos de demostración y almacenamiento local. Está orientada a dos perfiles: **docente (administrador)** y **estudiante (colaborador)**.

## Integrantes

- **Giovanni Orellana**
- **Vicente Vilches**

## Funcionalidades implementadas

### Acceso y perfiles
- Pantalla de bienvenida e ingreso mediante cuentas ficticias de demostración.
- Dos perfiles con opciones diferenciadas: docente y estudiante.
- Opción para cerrar sesión y volver a seleccionar un perfil.

> El acceso es demostrativo: no implementa autenticación con contraseña ni un servicio de usuarios real.

### Sectores del huerto
- Consultar los sectores registrados.
- Crear, editar y eliminar sectores desde el perfil docente.
- Registrar el nombre y la ubicación de cada sector.
- Validar los datos obligatorios y evitar eliminar sectores que tengan cultivos o tareas asociados.

### Cultivos
- Consultar cultivos y su sector asociado.
- Crear, editar y eliminar cultivos desde el perfil docente.
- Registrar el nombre del cultivo y vincularlo a un sector existente.
- Guardar una fecha inicial de siembra y un estado inicial al crear el registro.
- Evitar eliminar cultivos que todavía tengan tareas relacionadas.

### Tareas y evidencias
- Crear, editar y eliminar tareas desde el perfil docente, indicando descripción, sector y fecha programada.
- Visualizar las tareas y sus estados.
- Entregar una tarea desde el perfil estudiante con un comentario y una fotografía obligatoria.
- Capturar fotografías con la cámara o seleccionar una imagen mediante el selector de documentos del dispositivo.
- Mostrar una vista previa de la evidencia fotográfica.
- Revisar, aprobar o rechazar entregas desde el perfil docente y registrar el resultado de la revisión.
- Validaciones de formularios y mensajes para campos incorrectos o errores de operación.

> En esta versión de demostración las tareas nuevas se asignan a un estudiante ficticio predefinido; todavía no existe un selector de responsables.

### Lecturas simuladas
- Consultar mediciones ficticias de humedad del suelo y temperatura.
- Simular una variación de humedad mediante una interacción de prueba.
- No se utilizan sensores físicos ni se controla un sistema de riego real.

## Tecnologías utilizadas

- **Kotlin** para el desarrollo de la aplicación.
- **Android Studio** como entorno de desarrollo.
- **Jetpack Compose** y **Material Design 3** para la interfaz.
- **Navigation Compose** para la navegación entre pantallas.
- **Room / SQLite** para almacenar los datos localmente.
- **MVVM, Repository, ViewModel y StateFlow** para organizar los datos y el estado de la interfaz.
- **Coil** para visualizar imágenes.
- **Gradle Wrapper** para gestionar la compilación y las dependencias.

## Base de datos

La base de datos local utiliza el archivo `registro_huerto_escolar.db` y tres tablas principales:

| Tabla | Información almacenada |
| --- | --- |
| `zonas_huerto` | Sectores del huerto y sus datos generales. |
| `plantaciones` | Cultivos asociados a los sectores. |
| `actividades_huerto` | Tareas, estados, comentarios, fotografías y revisiones. |

Los datos se conservan en el dispositivo al cerrar y volver a abrir la aplicación. **Nota:** esta versión utiliza un nombre de base de datos distinto del anterior (`huerto_db.db`), por lo que no migra automáticamente los registros antiguos.

## Requisitos para ejecutar

- Android Studio con soporte para las versiones de Android Gradle Plugin y Kotlin configuradas en el proyecto.
- JDK 17 configurado para Gradle.
- SDK de Android compatible con la configuración del proyecto (compilación con API 36.1).
- Emulador o dispositivo Android con **API 26 (Android 8.0) o superior**.
- Conexión a internet durante la primera sincronización de dependencias, si aún no están descargadas.

## Pasos de instalación y ejecución

1. Clona el repositorio o descarga el código fuente:

   ```bash
   git clone https://github.com/Hikarifire/Huerto_escolar.git
   ```

2. Abre **Android Studio** y selecciona **Open**.
3. Selecciona la carpeta del proyecto `Huerto_escolar` (la que contiene `settings.gradle.kts`).
4. Espera la sincronización de Gradle y acepta la instalación de componentes del SDK si Android Studio lo solicita.
5. Inicia un emulador Android o conecta un dispositivo con depuración USB habilitada.
6. Presiona **Run ▶** y elige la aplicación `app`.
7. En la pantalla inicial selecciona el perfil ficticio **docente** o **estudiante** para probar las funciones disponibles.

**Compilación opcional desde la terminal (Windows):**

```powershell
.\gradlew.bat assembleDebug
```

**Compilación opcional desde Linux/macOS:**

```bash
./gradlew assembleDebug
```

## Flujo de demostración sugerido

1. Ingresa como docente y crea un sector.
2. Agrega un cultivo a ese sector.
3. Crea una tarea con una fecha programada.
4. Cierra sesión e ingresa como estudiante.
5. Selecciona la tarea, añade una foto y un comentario, y envía la evidencia.
6. Vuelve al perfil docente para aprobarla o rechazarla.
7. Revisa la pestaña de lecturas para observar los valores simulados.

## Estado del proyecto

El repositorio contiene el código fuente y el Gradle Wrapper. La compilación y la ejecución deben comprobarse en Android Studio. Se trata de un **prototipo académico**, no de una aplicación lista para uso institucional con cuentas reales.

**Repositorio:** https://github.com/Hikarifire/Huerto_escolar