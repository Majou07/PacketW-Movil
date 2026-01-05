package uv.tc.packetworld

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.packetworld.dto.RSAutenticacionConductor
import uv.tc.packetworld.pojo.Colaborador
import uv.tc.packetworld.util.Conexion

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener {
            verificarCredenciales()
        }
    }

    private fun verificarCredenciales() {
        if (camposValidos()) {
            consumirAPI(
                binding.etNumeroPersonal.text.toString(),
                binding.etPassword.text.toString()
            )
        }
    }

    private fun camposValidos(): Boolean {
        var valido = true

        if (binding.etNumeroPersonal.text.isEmpty()) {
            binding.etNumeroPersonal.error = "Número de personal obligatorio"
            valido = false
        }

        if (binding.etPassword.text.isEmpty()) {
            binding.etPassword.error = "Contraseña obligatoria"
            valido = false
        }

        return valido
    }

    private fun consumirAPI(numeroPersonal: String, password: String) {

        Ion.getDefault(this@LoginActivity)
            .conscryptMiddleware.enable(false)

        Ion.with(this@LoginActivity)
            .load(
                "POST",
                "${Conexion().URL_API}autenticacion/conductor"
            )
            .setHeader(
                "Content-Type",
                "application/x-www-form-urlencoded"
            )
            .setBodyParameter("numeroPersonal", numeroPersonal)
            .setBodyParameter("contrasena", password)
            .asString()
            .setCallback { e, result ->
                if (e == null) {
                    serializarRespuesta(result)
                } else {
                    Toast.makeText(
                        this,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("API_ERROR", e.toString())
                }
            }
    }

    private fun serializarRespuesta(json: String) {
        try {
            val gson = Gson()
            val respuesta =
                gson.fromJson(json, RSAutenticacionConductor::class.java)

            if (!respuesta.error) {
                Toast.makeText(
                    this,
                    "Bienvenido ${respuesta.conductor!!.nombre}",
                    Toast.LENGTH_LONG
                ).show()

                irPantallaPrincipal(json)
            } else {
                Toast.makeText(
                    this,
                    respuesta.mensaje,
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al procesar respuesta",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun irPantallaPrincipal(json: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("conductor", json)
        startActivity(intent)
        finish()
    }
}