package uv.tc.packetworld

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import uv.tc.packetworld.databinding.ActivityMainBinding
import uv.tc.packetworld.dto.RSAutenticacionConductor
import uv.tc.packetworld.pojo.Colaborador
import uv.tc.packetworld.pojo.Envio
import uv.tc.packetworld.util.Conexion


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var conductor: Colaborador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (cargarConductorDesdeIntent()) {
            cargarEnviosAsignados()
        } else {
            Toast.makeText(
                this,
                "Error de sesión",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun cargarConductorDesdeIntent(): Boolean {
        return try {
            val json = intent.getStringExtra("conductor") ?: return false
            val gson = Gson()
            val respuesta =
                gson.fromJson(json, RSAutenticacionConductor::class.java)

            conductor = respuesta.conductor ?: return false

            binding.tvNombreCompleto.text =
                "${conductor.nombre} ${conductor.apellidoPaterno} ${conductor.apellidoMaterno ?: ""}"
            binding.tvCorreo.text = conductor.correoElectronico

            binding.btnEditarPerfil.setOnClickListener {
                val intent = Intent(this, EditarColaboradorActivity::class.java)
                intent.putExtra("conductor", gson.toJson(conductor))
                startActivity(intent)
            }

            true
        } catch (e: Exception) {
            false
        }
    }


    private fun mostrarInformacionConductor() {
        try {
            val json = intent.getStringExtra("conductor")
            val gson = Gson()
            val respuesta =
                gson.fromJson(json, RSAutenticacionConductor::class.java)

            conductor = respuesta.conductor!!

            binding.tvNombreCompleto.text =
                "${conductor.nombre} ${conductor.apellidoPaterno} ${conductor.apellidoMaterno ?: ""}"

            binding.tvCorreo.text = conductor.correoElectronico

            binding.btnEditarPerfil.setOnClickListener {
                val intent = Intent(this, EditarColaboradorActivity::class.java)
                intent.putExtra("conductor", gson.toJson(conductor))
                startActivity(intent)
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar información",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarEnviosAsignados() {

        Ion.with(this@MainActivity)
            .load(
                "GET",
                "${Conexion().URL_API}envios/conductor/${conductor.idColaborador}"
            )
            .asString()
            .setCallback { e, result ->
                if (e == null) {
                    procesarEnvios(result)
                } else {
                    Toast.makeText(
                        this,
                        "Error al cargar envíos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun procesarEnvios(json: String) {
        try {
            val lista = ArrayList<Envio>()
            val arr = JSONArray(json)

            for (i in 0 until arr.length()) {
                val fila = arr.getJSONArray(i)
                lista.add(
                    Envio(
                        fila.getString(0),
                        fila.getString(1),
                        fila.getString(2)
                    )
                )
            }

            //mostramos la lista
            binding.listEnvios.adapter = AdaptadorEnvio(this, lista)

            binding.listEnvios.setOnItemClickListener { _, _, position, _ ->
                val envioSeleccionado = lista[position]

                val intent = Intent(this, DetalleEnvioActivity::class.java)
                intent.putExtra("guia", envioSeleccionado.numeroGuia)
                startActivity(intent)
            }


        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al procesar envíos",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}