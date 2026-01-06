package uv.tc.packetworld

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.packetworld.databinding.ActivityEditarColaboradorBinding
import uv.tc.packetworld.dto.Respuesta
import uv.tc.packetworld.pojo.Colaborador
import uv.tc.packetworld.util.Conexion

class EditarColaboradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarColaboradorBinding
    private lateinit var conductor: Colaborador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarColaboradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatos()
        binding.btnActualizar.setOnClickListener {
            actualizarDatos()
        }
    }

    private fun cargarDatos() {
        val json = intent.getStringExtra("conductor")
        val gson = Gson()
        conductor = gson.fromJson(json, Colaborador::class.java)

        binding.etNombre.setText(conductor.nombre)
        binding.etApellidoPaterno.setText(conductor.apellidoPaterno)
        binding.etApellidoMaterno.setText(conductor.apellidoMaterno)
        binding.etCorreo.setText(conductor.correoElectronico)
    }

    private fun actualizarDatos() {

        conductor.nombre = binding.etNombre.text.toString()
        conductor.apellidoPaterno = binding.etApellidoPaterno.text.toString()
        conductor.apellidoMaterno = binding.etApellidoMaterno.text.toString()
        conductor.correoElectronico = binding.etCorreo.text.toString()

        val gson = Gson()
        val jsonConductor = gson.toJson(conductor)

        Ion.with(this@EditarColaboradorActivity)
            .load(
                "PUT",
                "${Conexion().URL_API}colaborador/editar"
            )
            .setHeader("Content-Type", "application/json")
            .setStringBody(jsonConductor)
            .asString()
            .setCallback { e, result ->
                if (e == null) {
                    verificarRespuesta(result)
                } else {
                    Toast.makeText(
                        this,
                        "Error al actualizar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun verificarRespuesta(json: String) {
        try {
            val gson = Gson()
            val respuesta =
                gson.fromJson(json, Respuesta::class.java)

            Toast.makeText(
                this,
                respuesta.mensaje,
                Toast.LENGTH_LONG
            ).show()

            if (!respuesta.error) {
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al procesar respuesta",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}