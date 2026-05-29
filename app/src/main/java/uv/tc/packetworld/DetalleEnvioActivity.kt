package uv.tc.packetworld

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import org.json.JSONObject
import uv.tc.packetworld.util.Conexion

class DetalleEnvioActivity : AppCompatActivity() {

    private lateinit var tvGuia: TextView
    private lateinit var tvDestino: TextView
    private lateinit var tvSucursal: TextView
    private lateinit var tvCliente: TextView
    private lateinit var tvDestinatario: TextView
    private lateinit var tvPaquetes: TextView
    private lateinit var tvEstatus: TextView

    private lateinit var spEstatus: Spinner
    private lateinit var etComentario: EditText
    private lateinit var btnActualizar: Button

    private var idEnvio: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_envio)

        tvGuia = findViewById(R.id.tvGuia)
        tvDestino = findViewById(R.id.tvDestino)
        tvSucursal = findViewById(R.id.tvSucursal)
        tvCliente = findViewById(R.id.tvCliente)
        tvDestinatario = findViewById(R.id.tvDestinatario)
        tvPaquetes = findViewById(R.id.tvPaquetes)
        tvEstatus = findViewById(R.id.tvEstatus)

        spEstatus = findViewById(R.id.spEstatus)
        etComentario = findViewById(R.id.etComentario)
        btnActualizar = findViewById(R.id.btnActualizarEstatus)

        idEnvio = intent.getIntExtra("ID_ENVIO", -1)

        if (idEnvio == -1) {
            Toast.makeText(this, "ID de envío inválido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        configurarSpinner()

        cargarDetalleEnvio()
        cargarPaquetes()

        btnActualizar.setOnClickListener {
            actualizarEstatus()
        }
    }

    // ---------------- SPINNER ----------------
    private fun configurarSpinner() {
        val estatus = arrayOf(
            "recibido en sucursal",
            "procesado",
            "en transito",
            "detenido",
            "entregado",
            "cancelado"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estatus)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEstatus.adapter = adapter
    }

    // ---------------- DETALLE ----------------
    private fun cargarDetalleEnvio() {

        Ion.with(this)
            .load("GET", "${Conexion().URL_API}envio/detalle/$idEnvio")
            .setTimeout(4000)
            .asString()
            .setCallback { e, result ->

                if (e == null && result != null) {
                    mostrarDetalle(result)
                } else {
                    limpiarPantallaSinConexion()
                    Toast.makeText(this, "Sin conexión con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun mostrarDetalle(json: String) {

        try {
            val obj = JSONObject(json)

            tvGuia.text = "Guía: ${obj.getString("numeroGuia")}"

            val direccion = "${obj.getString("destinoCalle")} ${obj.getString("destinoNumero")}, " +
                    "${obj.getString("destinoColonia")}, CP ${obj.getString("destinoCodigoPostal")}, " +
                    "${obj.getString("destinoCiudad")}, ${obj.getString("destinoEstado")}"

            tvDestino.text = "Destino: $direccion"

            tvSucursal.text = "Sucursal origen: ${obj.getString("sucursalOrigen")}"

            val destinatario =
                "${obj.getString("destinatarioNombre")} " +
                        "${obj.getString("destinatarioApPaterno")} " +
                        "${obj.getString("destinatarioApMaterno")}"

            tvDestinatario.text = "Destinatario: $destinatario"

            tvEstatus.text = "Estatus: ${obj.getString("estatusEnvio")}"

            val cliente =
                "${obj.getString("nombreCliente")}\n" +
                        "Tel: ${obj.optString("telefono", "N/D")}\n" +
                        "Correo: ${obj.optString("correoElectronico", "N/D")}"

            tvCliente.text = "Cliente:\n$cliente"

        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar detalle", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- PAQUETES ----------------
    private fun cargarPaquetes() {

        Ion.with(this)
            .load("GET", "${Conexion().URL_API}paquete/obtener-por-envio/$idEnvio")
            .setTimeout(4000)
            .asString()
            .setCallback { e, result ->

                if (e == null && result != null) {
                    mostrarPaquetes(result)
                } else {
                    tvPaquetes.text = "Paquetes: Sin conexión"
                }
            }
    }

    private fun mostrarPaquetes(json: String) {

        try {
            val array = JSONArray(json)

            if (array.length() == 0) {
                tvPaquetes.text = "Paquetes: No registrados"
                return
            }

            val lista = mutableListOf<String>()

            for (i in 0 until array.length()) {
                val p = array.getJSONObject(i)

                lista.add(
                    "• ${p.getString("descripcion")} " +
                            "(${p.getDouble("peso")} kg, " +
                            "${p.getDouble("alto")}x${p.getDouble("ancho")}x${p.getDouble("profundidad")} cm)"
                )
            }

            tvPaquetes.text = lista.joinToString("\n")

        } catch (e: Exception) {
            tvPaquetes.text = "Error paquetes"
        }
    }

    // ---------------- ACTUALIZAR ESTATUS ----------------
    private fun actualizarEstatus() {

        val estatus = spEstatus.selectedItem.toString()
        val comentario = etComentario.text.toString()

        if ((estatus == "detenido" || estatus == "cancelado") && comentario.isEmpty()) {
            Toast.makeText(this, "Comentario obligatorio", Toast.LENGTH_LONG).show()
            return
        }

        val idEstatus = when (estatus) {
            "recibido en sucursal" -> 1
            "procesado" -> 2
            "en transito" -> 3
            "detenido" -> 4
            "entregado" -> 5
            "cancelado" -> 6
            else -> 1
        }

        Ion.with(this)
            .load("PUT", "${Conexion().URL_API}envio/actualizar-estatus")
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .setBodyParameter("idEnvio", idEnvio.toString())
            .setBodyParameter("idEstatus", idEstatus.toString())
            .setBodyParameter("comentario", comentario)
            .setBodyParameter("idColaborador", "1")
            .asString()
            .setCallback { e, _ ->

                if (e == null) {
                    Toast.makeText(this, "Estatus actualizado", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ---------------- LIMPIEZA ----------------
    private fun limpiarPantallaSinConexion() {

        tvGuia.text = "Guía: Sin conexión"
        tvDestino.text = "Destino: Sin conexión"
        tvSucursal.text = "Sucursal: Sin conexión"
        tvCliente.text = "Cliente: Sin conexión"
        tvDestinatario.text = "Destinatario: Sin conexión"
        tvPaquetes.text = "Paquetes: Sin conexión"
        tvEstatus.text = "Estatus: Sin conexión"

        etComentario.setText("")
        spEstatus.setSelection(0)
    }
}