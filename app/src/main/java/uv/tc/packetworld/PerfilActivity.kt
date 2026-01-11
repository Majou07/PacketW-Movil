package uv.tc.packetworld

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.packetworld.pojo.Colaborador
import uv.tc.packetworld.util.Conexion
import uv.tc.packetworld.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private var idConductor = -1
    private var conductor: Colaborador? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idConductor = intent.getIntExtra("ID_CONDUCTOR", -1)
        if (idConductor == -1) {
            finish()
            return
        }

        cargarPerfil()

        binding.btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, EditarColaboradorActivity::class.java)
            intent.putExtra("ID_CONDUCTOR", idConductor)
            startActivity(intent)
        }

        binding.btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun cargarPerfil() {
        Ion.with(this)
            .load("${Conexion().URL_API}colaborador/$idConductor")
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val colaborador = Gson().fromJson(result, Colaborador::class.java)
                        colaborador?.let {
                            conductor = it
                            binding.tvNombre.text = "${it.nombre} ${it.apellidoPaterno}"
                            binding.tvCorreo.text = it.correoElectronico
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(
                            this,
                            "Error al procesar perfil: ${ex.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_LONG).show()
                }
            }
    }

}