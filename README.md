# Huerto Escolar para Android

Aplicación con Kotlin, Jetpack Compose, Room y Retrofit. Integrantes: Giovanni Orellana y Vicente Vilches.

## Ejecutar

1. Levanta MySQL y Spring Boot según el README del backend.
2. Abre este proyecto en Android Studio con SDK 36.1 y un JDK compatible con AGP 9.
3. Ejecuta la app en un emulador Android 8.0 o superior. La URL predeterminada del backend es `http://10.0.2.2:8080/`. Para un teléfono físico, configura `huertoUrl=http://IP_DEL_PC:8080/` en `gradle.properties`.

Backend en https://github.com/Hikarifire/Back_Huerto.

## Administración de cuentas

Configura la primera cuenta `admin@huerto.cl` siguiendo el README del backend. Inicia sesión y abre **Más → Gestionar cuentas**. Elige el perfil, completa los datos y confirma la contraseña. La cuenta se guarda en el servidor y el estudiante nuevo queda disponible para asignarle tareas.

El administrador también puede gestionar sectores, cultivos, tareas, revisiones, reportes y lecturas desde las mismas pantallas del docente. El registro público crea cuentas de estudiante.

## Cuentas de demostración

| Rol | Correo | Clave ficticia |
|---|---|---|
| Docente | `docente@huerto.cl` | `Docente123!` |
| Estudiante 1 | `estudiante1@huerto.cl` | `Estudiante123!` |
| Estudiante 2 | `estudiante2@huerto.cl` | `Estudiante123!` |
| Estudiante 3 | `estudiante3@huerto.cl` | `Estudiante123!` |

El registro público crea cuentas de estudiante. El perfil y los permisos se resuelven en Spring Boot. Para desactivar los usuarios y datos ficticios en una base nueva, configura `SEED_DEMO_DATA=false` antes del primer arranque.

## API e integración

Android usa Kotlin, Jetpack Compose, MVVM, StateFlow, Room y Retrofit. MySQL almacena los registros compartidos. Room guarda los borradores por usuario y tarea. Spring Boot expone la API propia y aplica las reglas de autorización. El proyecto incluye un servicio Spring Boot desplegable.

| Componente | Operaciones |
|---|---|
| Sesiones | `POST /api/auth/login`, `POST /api/auth/registro`, `GET /api/auth/me`, `POST /api/auth/logout` |
| Alta de cuentas | `POST /api/usuarios/cuentas/DOCENTE` y `POST /api/usuarios/cuentas/ALUMNO`, solo `ADMIN` |
| Usuarios | `GET /api/usuarios/estudiantes` |
| Sectores, cultivos y tareas | `GET/POST /api/{recurso}`, `GET/PUT/DELETE /api/{recurso}/{id}` |
| Tareas y evidencias | `POST /api/tareas/{id}/iniciar`, `GET/POST /api/tareas/{id}/evidencias`, `POST /api/evidencias/{id}/revision`, `GET /api/evidencias/{id}/foto` |
| Reportes | `GET/POST /api/reportes`, `PATCH /api/reportes/{id}`, `GET /api/reportes/{id}/foto` |
| Sensores simulados | `GET /api/lecturas`, `POST /api/lecturas/simuladas` |
| Open-Meteo | `GET https://api.open-meteo.com/v1/forecast` con coordenadas fijas de San Bernardo, `current=temperature_2m,relative_humidity_2m,precipitation` y `timezone=America/Santiago` |

Las fotos y los reportes están sujetos a los permisos del recurso asociado. Una tarea con evidencias se conserva para mantener sus revisiones. Las bajas de sectores y cultivos requieren que estén libres de referencias.

## Pruebas

```bash
./gradlew :app:testDebugUnitTest :app:koverHtmlReportDebug :app:koverVerifyDebug
```

## Generar la APK firmada

En Android Studio abre **Build → Generate Signed Bundle / APK → APK**. Pulsa **Create new** si vas a generar tu propia clave académica, elige **release** y verifica el resultado con `apksigner verify --verbose --print-certs app-release.apk`. Guarda la llave y su contraseña para futuras compilaciones.
