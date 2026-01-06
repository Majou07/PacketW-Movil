package uv.tc.packetworld

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.koushikdutta.ion.Ion
import org.json.JSONObject
import uv.tc.packetworld.util.Conexion

class DetalleEnvioActivity : AppCompatActivity() {

    private lateinit var tvGuia: TextView
    private lateinit var tvDestino: TextView
    private lateinit var tvSucursal: TextView
    private lateinit var tvCliente: TextView
    private lateinit var spEstatus: Spinner
    private lateinit var etComentario: EditText
    private lateinit var btnActualizar: Button

    private lateinit var numeroGuia: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_envio)

        tvGuia = findViewById(R.id.tvGuia)
        tvDestino = findViewById(R.id.tvDestino)
        tvSucursal = findViewById(R.id.tvSucursal)
        tvCliente = findViewById(R.id.tvCliente)
        spEstatus = findViewById(R.id.spEstatus)
        etComentario = findViewById(R.id.etComentario)
        btnActualizar = findViewById(R.id.btnActualizarEstatus)

        numeroGuia = intent.getStringExtra("guia")!!

        configurarSpinner()
        cargarDetalleEnvio()

        btnActualizar.setOnClickListener {
            actualizarEstatus()
        }
    }

    private fun configurarSpinner() {
        val estatus = arrayOf(
            "en transito", "detenido", "entregado", "cancelado"
        )

        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, estatus
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spEstatus.adapter = adapter
    }

    private fun cargarDetalleEnvio() {
        Ion.with(this)
            .load(
                "GET",
                "${Conexion().URL_API}envios/$numeroGuia"
            )
            .asString().setCallback { e, result ->
                if (e == null && result != null) {
                    mostrarDetalle(result)
                } else {
                    Toast.makeText(this, "Error al cargar detalle", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun mostrarDetalle(json: String) {
        try {
            val obj = JSONObject(json)

            tvGuia.text = "Guía: ${obj.getString("numeroGuia")}"
            tvDestino.text =
                "Destino: ${obj.getString("destinoCalle")} ${obj.getString("destinoNumero")}, " +
                        "${obj.getString("destinoColonia")}"

            tvSucursal.text =
                "Sucursal origen: ${obj.getString("codigoSucursalOrigen")}"

            val cliente = obj.getJSONObject("cliente")
            tvCliente.text =
                "Cliente: ${cliente.getString("nombre")} | ${cliente.getString("telefono")} " +
                        "| ${cliente.getString("correoElectronico")}"

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al procesar datos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun actualizarEstatus() {
        val estatusSeleccionado = spEstatus.selectedItem.toString()
        val comentario = etComentario.text.toString()

        if (
            (estatusSeleccionado == "detenido" ||
                    estatusSeleccionado == "cancelado") && comentario.isEmpty()
        ) {
            etComentario.error = "Comentario obligatorio para este estatus"
            return
        }

        val json = """ {
          "idEstatusEnvio": ${obtenerIdEstatus(estatusSeleccionado)},
          "idColaborador": 1,
          "comentario": "$comentario"
        } """.trimIndent()

        Ion.with(this)
            .load(
                "PUT",
                "${Conexion().URL_API}envios/$numeroGuia/estatus"
            )
            .setHeader("Content-Type", "application/json")
            .setStringBody(json)
            .asString()
            .setCallback { e, _ ->
                if (e == null) {
                    Toast.makeText(
                        this,
                        "Estatus actualizado correctamente",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Error al actualizar estatus",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun obtenerIdEstatus(nombre: String): Int {
        return when (nombre) {
            "en transito" -> 3
            "detenido" -> 4
            "entregado" -> 5
            "cancelado" -> 6
            else -> 3
        }
    }
}
