package cl.duoc.fotomotion.data.local

import android.content.Context
import cl.duoc.fotomotion.domain.model.CuentaHuerto
import cl.duoc.fotomotion.domain.model.TipoCuenta
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GestorUsuariosLocal(contexto: Context) {
    private val archivoUsuarios = File(contexto.filesDir, "usuarios_huerto.json")

    private val cuentasPredeterminadasDocente = listOf(
        CuentaHuerto("USR-001", "Profesor Carlos Silva", TipoCuenta.DOCENTE),
    )

    private val cuentasPredeterminadasAlumno = listOf(
        CuentaHuerto("USR-002", "Estudiante Mateo Rojas", TipoCuenta.ALUMNO),
        CuentaHuerto("USR-003", "Estudiante Sofía Castro", TipoCuenta.ALUMNO),
        CuentaHuerto("USR-004", "Estudiante Lucas Morales", TipoCuenta.ALUMNO),
        CuentaHuerto("USR-005", "Estudiante Camila Fuentes", TipoCuenta.ALUMNO),
    )

    fun cargarUsuarios(): List<CuentaHuerto> {
        if (!archivoUsuarios.exists()) {
            guardarUsuarios(cuentasPredeterminadasDocente + cuentasPredeterminadasAlumno)
            return cuentasPredeterminadasDocente + cuentasPredeterminadasAlumno
        }
        return try {
            val contenido = archivoUsuarios.readText()
            val jsonRoot = JSONObject(contenido)
            val listaResultado = mutableListOf<CuentaHuerto>()

            val arrayProfesores = jsonRoot.optJSONArray("profesores") ?: JSONArray()
            for (i in 0 until arrayProfesores.length()) {
                val obj = arrayProfesores.getJSONObject(i)
                listaResultado.add(
                    CuentaHuerto(
                        codigoCuenta = obj.getString("codigoCuenta"),
                        nombreCuenta = obj.getString("nombreCuenta"),
                        tipoCuenta = TipoCuenta.DOCENTE,
                    ),
                )
            }

            val arrayEstudiantes = jsonRoot.optJSONArray("estudiantes") ?: JSONArray()
            for (i in 0 until arrayEstudiantes.length()) {
                val obj = arrayEstudiantes.getJSONObject(i)
                listaResultado.add(
                    CuentaHuerto(
                        codigoCuenta = obj.getString("codigoCuenta"),
                        nombreCuenta = obj.getString("nombreCuenta"),
                        tipoCuenta = TipoCuenta.ALUMNO,
                    ),
                )
            }
            if (listaResultado.isEmpty()) cuentasPredeterminadasDocente + cuentasPredeterminadasAlumno else listaResultado
        } catch (_: Exception) {
            cuentasPredeterminadasDocente + cuentasPredeterminadasAlumno
        }
    }

    fun guardarUsuarios(cuentas: List<CuentaHuerto>) {
        try {
            val jsonRoot = JSONObject()

            val arrayProfesores = JSONArray()
            cuentas.filter { it.tipoCuenta == TipoCuenta.DOCENTE }.forEach { docente ->
                val obj = JSONObject().apply {
                    put("codigoCuenta", docente.codigoCuenta)
                    put("nombreCuenta", docente.nombreCuenta)
                    put("tipoCuenta", docente.tipoCuenta.name)
                }
                arrayProfesores.put(obj)
            }

            val arrayEstudiantes = JSONArray()
            cuentas.filter { it.tipoCuenta == TipoCuenta.ALUMNO }.forEach { estudiante ->
                val obj = JSONObject().apply {
                    put("codigoCuenta", estudiante.codigoCuenta)
                    put("nombreCuenta", estudiante.nombreCuenta)
                    put("tipoCuenta", estudiante.tipoCuenta.name)
                }
                arrayEstudiantes.put(obj)
            }

            jsonRoot.put("profesores", arrayProfesores)
            jsonRoot.put("estudiantes", arrayEstudiantes)

            archivoUsuarios.writeText(jsonRoot.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun agregarUsuario(nuevaCuenta: CuentaHuerto): List<CuentaHuerto> {
        val listaActual = cargarUsuarios().toMutableList()
        if (listaActual.none { it.codigoCuenta == nuevaCuenta.codigoCuenta }) {
            listaActual.add(nuevaCuenta)
            guardarUsuarios(listaActual)
        }
        return listaActual
    }
}
