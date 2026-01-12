package uv.tc.packetworld

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

        // Referencias a los TextView
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


        // Recuperar idEnvio desde el intent
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

    private fun configurarSpinner() {
        val estatus = arrayOf(
            "recibido en sucursal","procesado","en transito", "detenido", "entregado", "cancelado"
        )

        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, estatus
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEstatus.adapter = adapter
    }


    private fun cargarDetalleEnvio() {
        Ion.with(this)
            .load("GET", "${Conexion().URL_API}envio/detalle/$idEnvio")
            .asString(Charsets.UTF_8)
            .setCallback { e, result ->
                if (e == null && result != null) {
                    mostrarDetalle(result)
                } else {
                    Toast.makeText(this, "Error al cargar el detalle del envio", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun cargarPaquetes() {
        Ion.with(this)
            .load("GET", "${Conexion().URL_API}paquete/obtener-por-envio/$idEnvio")
            .asString(Charsets.UTF_8)
            .setCallback { e, result ->
                if (e == null && result != null) {
                    mostrarPaquetes(result)
                } else {
                    tvPaquetes.text = "Paquetes: No registrados"
                }
            }
    }

    private fun mostrarDetalle(json: String) {
        try {
            val obj = JSONObject(json)

            // Guía
            tvGuia.text = "Guía: ${obj.getString("numeroGuia")}"

            // Dirección completa
            val direccion = "${obj.getString("destinoCalle")} ${obj.getString("destinoNumero")}, " +
                    "${obj.getString("destinoColonia")}, CP ${obj.getString("destinoCodigoPostal")}, " +
                    "${obj.getString("destinoCiudad")}, ${obj.getString("destinoEstado")}"
            tvDestino.text = "Destino: $direccion"

            // Sucursal origen
            tvSucursal.text = "Sucursal origen: ${obj.getString("sucursalOrigen")}"

            // Destinatario
            val destinatario = "${obj.getString("destinatarioNombre")} " +
                    "${obj.getString("destinatarioApPaterno")} ${obj.getString("destinatarioApMaterno")}"
            tvDestinatario.text = "Destinatario: $destinatario"

            // Estatus
            tvEstatus.text = "Estatus: ${obj.getString("estatusEnvio")}"

            // Cliente remitente
            val clienteNombre = obj.getString("nombreCliente")
            val clienteTelefono = if (obj.has("telefono")) obj.getString("telefono") else "N/D"
            val clienteCorreo = if (obj.has("correoElectronico")) obj.getString("correoElectronico") else "N/D"
            tvCliente.text = "Cliente: $clienteNombre\nTeléfono: $clienteTelefono\nCorreo electrónico: $clienteCorreo"

        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar datos del envío", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarPaquetes(json: String) {
        try {
            val paquetesArray = JSONArray(json)
            if (paquetesArray.length() == 0) {
                tvPaquetes.text = "Paquetes: No registrados"
                return
            }

            val paquetesList = mutableListOf<String>()
            for (i in 0 until paquetesArray.length()) {
                val paquete = paquetesArray.getJSONObject(i)
                val descripcion = paquete.getString("descripcion")
                val peso = paquete.getDouble("peso")
                val alto = paquete.getDouble("alto")
                val ancho = paquete.getDouble("ancho")
                val profundidad = paquete.getDouble("profundidad")

                paquetesList.add("• $descripcion ($peso kg, ${alto}x${ancho}x${profundidad} cm)")
            }

            tvPaquetes.text = "Paquetes:\n${paquetesList.joinToString("\n")}"

        } catch (e: Exception) {
            tvPaquetes.text = "Error al procesar paquetes"
        }
    }

    private fun actualizarEstatus() {
        val estatusSeleccionado = spEstatus.selectedItem.toString().trim()
        val comentario = etComentario.text.toString().trim()

        // Validación: comentario obligatorio si estatus es detenido o cancelado
        if ((estatusSeleccionado == "detenido" || estatusSeleccionado == "cancelado") && comentario.isEmpty()) {
            Toast.makeText(this, "El comentario es obligatorio para el estatus seleccionado", Toast.LENGTH_LONG).show()
            return
        }

        val idEstatus = obtenerIdEstatus(estatusSeleccionado)
        val idColaborador = 1 // ← reemplaza con el ID real del conductor si tienes sesión

        Ion.with(this)
            .load("PUT", "${Conexion().URL_API}envio/actualizar-estatus")
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .setBodyParameter("idEnvio", idEnvio.toString())
            .setBodyParameter("idEstatus", idEstatus.toString())
            .setBodyParameter("comentario", comentario)
            .setBodyParameter("idColaborador", idColaborador.toString())
            .asString()
            .setCallback { e, result ->
                if (e == null) {
                    Toast.makeText(this, "Estatus actualizado correctamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar estatus", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun obtenerIdEstatus(nombre: String): Int {
        return when (nombre.lowercase()) {
            "recibido en sucursal" -> 1
            "procesado" -> 2
            "en transito" -> 3
            "detenido" -> 4
            "entregado" -> 5
            "cancelado" -> 6
            else -> 1 // Valor por defecto
        }
    }


}
