# Huerto Escolar – Ajustes visuales leves

Aplicación Android de gestión del huerto escolar. Se mantiene el flujo original de acceso, sectores, cultivos, tareas y lecturas.

Cambios de esta versión:
- Inicio de sesión: se agregó un recuadro de bienvenida y textos renovados.
- Paleta: verde azulado, terracota claro y fondo gris marfil.
- Room: la base ahora se llama `registro_huerto_escolar.db`; las tablas se llaman `zonas_huerto`, `plantaciones` y `actividades_huerto`.

> Importante: como el nombre de la base cambió, esta versión crea una base nueva al ejecutarse. No migra automáticamente registros del archivo `huerto_db.db` anterior.

Abre el proyecto con Android Studio y sincroniza Gradle. Los resultados de compilación y pruebas instrumentadas deben verificarse en Android Studio.
