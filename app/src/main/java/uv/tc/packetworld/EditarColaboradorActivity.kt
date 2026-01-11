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
    private var idConductor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarColaboradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idConductor = intent.getIntExtra("ID_CONDUCTOR", -1)
        if (idConductor == -1) {
            finish()
            return
        }

        cargarDatos()

        binding.btnActualizar.setOnClickListener { actualizarDatos() }
        binding.btnRegresar.setOnClickListener { finish() }
    }

    private fun cargarDatos() {
        Ion.with(this)
            .load("${Conexion().URL_API}obtener/$idConductor")
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    conductor = Gson().fromJson(result, Colaborador::class.java)
                    binding.etNombre.setText(conductor.nombre)
                    binding.etApellidoPaterno.setText(conductor.apellidoPaterno)
                    binding.etApellidoMaterno.setText(conductor.apellidoMaterno)
                    binding.etCorreo.setText(conductor.correoElectronico)
                    binding.etCurp.setText(conductor.curp)
                    binding.etContrasena.setText(conductor.contrasena)
                    binding.etNumeroLicencia.setText(conductor.numeroLicencia)
                } else {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun actualizarDatos() {
        conductor.nombre = binding.etNombre.text.toString()
        conductor.apellidoPaterno = binding.etApellidoPaterno.text.toString()
        conductor.apellidoMaterno = binding.etApellidoMaterno.text.toString()
        conductor.correoElectronico = binding.etCorreo.text.toString()

        Ion.with(this)
            .load("PUT", "${Conexion().URL_API}colaborador/editar")
            .setHeader("Content-Type", "application/json")
            .setStringBody(Gson().toJson(conductor))
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val r = Gson().fromJson(result, Respuesta::class.java)
                    Toast.makeText(this, r.mensaje, Toast.LENGTH_LONG).show()
                    if (!r.error) finish()
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_LONG).show()
                }
            }
    }
}
