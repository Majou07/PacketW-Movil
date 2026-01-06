package uv.tc.packetworld

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import uv.tc.packetworld.databinding.ActivityMainBinding
import uv.tc.packetworld.pojo.Envio
import uv.tc.packetworld.util.Conexion

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var idConductor: Int = -1
    private val listaEnvios = ArrayList<Envio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idConductor = intent.getIntExtra("ID_CONDUCTOR", -1)

        if (idConductor == -1) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        configurarMenu()
        cargarEnviosAsignados()
    }

    private fun configurarMenu() {
        binding.bottomNav.selectedItemId = R.id.nav_envios

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_envios -> true

                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("ID_CONDUCTOR", idConductor)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarEnviosAsignados() {
        Ion.with(this)
            .load("GET", "${Conexion().URL_API}envios/conductor/$idConductor")
            .asString()
            .setCallback { e, result ->
                if (e != null || result == null) {
                    Toast.makeText(this, "Error al cargar envíos", Toast.LENGTH_LONG).show()
                    return@setCallback
                }

                try {
                    listaEnvios.clear()
                    val array = JSONArray(result)

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        listaEnvios.add(
                            Envio(
                                obj.getString("numero_guia"),
                                obj.getString("destino"),
                                obj.getString("estatus")
                            )
                        )
                    }

                    binding.listEnvios.adapter =
                        AdaptadorEnvio(this, listaEnvios)

                    binding.listEnvios.setOnItemClickListener { _, _, pos, _ ->
                        val intent =
                            Intent(this, DetalleEnvioActivity::class.java)
                        intent.putExtra("guia", listaEnvios[pos].numeroGuia)
                        startActivity(intent)
                    }

                } catch (_: Exception) {
                    Toast.makeText(this, "Error al procesar envíos", Toast.LENGTH_LONG).show()
                }
            }
    }
}
