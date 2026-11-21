# Huerto Escolar — Android

Aplicación móvil para organizar sectores y cultivos, asignar tareas a estudiantes y revisar las evidencias del trabajo en el huerto.

**Integrantes** · Giovanni Orellana y Vicente Vilches

## Tecnologías

Kotlin, Jetpack Compose, Material Design 3, MVVM, StateFlow, Retrofit, Room, JUnit y Kover.

<<<<<<< HEAD
## Administración de cuentas

Configura la primera cuenta `admin@huerto.cl` siguiendo el README del backend. Inicia sesión y abre **Más → Gestionar cuentas**. Elige el perfil, completa los datos y confirma la contraseña. La cuenta se guarda en el servidor y el estudiante nuevo queda disponible para asignarle tareas.

El administrador también puede gestionar sectores, cultivos, tareas, revisiones, reportes y lecturas desde las mismas pantallas del docente. El registro público crea cuentas de estudiante.

## Cuentas de demostración
=======
## Ejecutar la aplicación
>>>>>>> 0b1145811284a8659a4f35e0b6139675a0ba4688

1. Inicia MySQL en XAMPP y luego Spring Boot. Sigue el [README del backend](https://github.com/Hikarifire/Back_Huerto).
2. Abre esta carpeta en **Android Studio** y sincroniza el proyecto con Gradle. La compilación usa el **SDK Android 36.1**. La app requiere **Android 8.0 (API 26)** o superior.
3. Inicia un emulador y pulsa **Run ▶**. Por defecto, Retrofit se conecta a `http://10.0.2.2:8080/`, la dirección del computador anfitrión desde el emulador.
4. Para probar en un teléfono físico en la misma red que el computador, agrega `huertoUrl=http://IP_DE_TU_PC:8080/` a la configuración local de Gradle y vuelve a compilar. Mantén la barra final de la URL.

## Funcionalidades por rol

| Rol | Funciones |
|---|---|
<<<<<<< HEAD
| Sesiones | `POST /api/auth/login`, `POST /api/auth/registro`, `GET /api/auth/me`, `POST /api/auth/logout` |
| Alta de cuentas | `POST /api/usuarios/cuentas/DOCENTE` y `POST /api/usuarios/cuentas/ALUMNO`, solo `ADMIN` |
| Usuarios | `GET /api/usuarios/estudiantes` |
| Sectores, cultivos y tareas | `GET/POST /api/{recurso}`, `GET/PUT/DELETE /api/{recurso}/{id}` |
| Tareas y evidencias | `POST /api/tareas/{id}/iniciar`, `GET/POST /api/tareas/{id}/evidencias`, `POST /api/evidencias/{id}/revision`, `GET /api/evidencias/{id}/foto` |
| Reportes | `GET/POST /api/reportes`, `PATCH /api/reportes/{id}`, `GET /api/reportes/{id}/foto` |
| Sensores simulados | `GET /api/lecturas`, `POST /api/lecturas/simuladas` |
| Open-Meteo | `GET https://api.open-meteo.com/v1/forecast` con coordenadas fijas de San Bernardo, `current=temperature_2m,relative_humidity_2m,precipitation` y `timezone=America/Santiago` |
=======
| Administrador | Gestiona sectores, cultivos, tareas, evidencias, reportes y lecturas. En **Más → Gestionar cuentas**, puede crear docentes y estudiantes. |
| Docente | Gestiona los recursos del huerto, asigna tareas, revisa evidencias y atiende reportes. |
| Estudiante | Consulta sus tareas, registra avances, adjunta fotos y envía evidencias. Puede reportar problemas del huerto. |
>>>>>>> 0b1145811284a8659a4f35e0b6139675a0ba4688

Room guarda los borradores de evidencia por usuario y tarea. Las tareas y evidencias enviadas se guardan en el servidor. La app permite tomar una foto con la cámara o elegirla desde la galería.

## Demo rápida

1. Inicia sesión como `admin@huerto.cl` con `AdminDemo123!` si el backend acaba de crear la cuenta de demostración. Si ya configuraste una clave propia, usa esa clave.
2. En **Más → Gestionar cuentas**, crea un docente y un estudiante. Crea un sector, registra un cultivo y asigna una tarea al estudiante.
3. Entra como estudiante. Inicia la tarea, agrega un comentario y una foto ficticia, y envía la evidencia. Debe aparecer **En revisión**.
4. Vuelve a entrar como administrador o docente. Revisa la evidencia y apruébala. Reabre la app y consulta la tarea para comprobar que conserva el estado **Aprobada**.

## APIs utilizadas

| API | Uso |
|---|---|
| Spring Boot | Retrofit consume la API propia para sesión, cuentas, sectores, cultivos, tareas, evidencias, reportes y lecturas. El listado de rutas está en el [README del backend](https://github.com/Hikarifire/Back_Huerto). |
| Open-Meteo | `GET https://api.open-meteo.com/v1/forecast` para mostrar temperatura, humedad relativa y precipitación de San Bernardo en la pantalla **Clima**. |

## Pruebas unitarias y cobertura

Abre PowerShell en la carpeta que contiene `gradlew.bat` y ejecuta:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:koverHtmlReportDebug :app:koverVerifyDebug
```

La versión verificada del proyecto pasó **17 pruebas unitarias**. Kover mide la lógica de `HuertoStore` y `Reglas`, y `koverVerifyDebug` exige un mínimo de **80 %**.

- Resultado de pruebas en `app/build/reports/tests/testDebugUnitTest/index.html`.
- Informe de cobertura en `app/build/reports/kover/htmlDebug/index.html`.

## Generar la APK firmada

1. Abre **Build → Generate Signed Bundle / APK → APK** en Android Studio.
2. Selecciona tu `.jks` académico o pulsa **Create new** para generar uno durante la defensa.
3. Elige **release** y genera la APK.
4. Comprueba la firma con `apksigner verify --verbose --print-certs app-release.apk`, usando la ruta al archivo generado.

Guarda el `.jks` y su contraseña para futuras compilaciones. Entrega los archivos y la evidencia de firma por el canal indicado en la pauta.
