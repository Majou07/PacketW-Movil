package uv.tc.packetworld

import android.content.Intent
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

        // Botón regresar (flecha)
        binding.btnRegresar.setOnClickListener {
            finish()
        }

        // Botón cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        cargarPerfil()

        binding.btnActualizar.setOnClickListener {
            actualizarPerfil()
        }
    }

    private fun cargarPerfil() {
        val idConductor = intent.getIntExtra("ID_CONDUCTOR", -1)

        if (idConductor == -1) {
            Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Ion.with(this)
            .load("${Conexion().URL_API}colaborador/$idConductor")
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val gson = Gson()
                    conductor = gson.fromJson(result, Colaborador::class.java)

                    binding.etNombre.setText(conductor.nombre)
                    binding.etApellidoPaterno.setText(conductor.apellidoPaterno)
                    binding.etApellidoMaterno.setText(conductor.apellidoMaterno)
                    binding.etCorreo.setText(conductor.correoElectronico)
                } else {
                    Toast.makeText(this, "No se pudo cargar el perfil", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun actualizarPerfil() {

        conductor.nombre = binding.etNombre.text.toString()
        conductor.apellidoPaterno = binding.etApellidoPaterno.text.toString()
        conductor.apellidoMaterno = binding.etApellidoMaterno.text.toString()
        conductor.correoElectronico = binding.etCorreo.text.toString()

        val json = Gson().toJson(conductor)

        Ion.with(this)
            .load("PUT", "${Conexion().URL_API}colaborador/editar")
            .setHeader("Content-Type", "application/json")
            .setStringBody(json)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val respuesta = Gson().fromJson(result, Respuesta::class.java)
                    Toast.makeText(this, respuesta.mensaje, Toast.LENGTH_LONG).show()
                    if (!respuesta.error) finish()
                } else {
                    Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun cerrarSesion() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
