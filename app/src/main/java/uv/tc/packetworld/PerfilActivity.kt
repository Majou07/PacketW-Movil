package uv.tc.packetworld

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.packetworld.databinding.ActivityPerfilBinding
import uv.tc.packetworld.pojo.Colaborador
import uv.tc.packetworld.util.Conexion

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private var idConductor = -1
    private lateinit var conductor: Colaborador

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
                    conductor = Gson().fromJson(result, Colaborador::class.java)
                    binding.tvNombre.text =
                        "${conductor.nombre} ${conductor.apellidoPaterno}"
                    binding.tvCorreo.text = conductor.correoElectronico
                } else {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_LONG).show()
                }
            }
    }
}