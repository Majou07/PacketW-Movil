package uv.tc.packetworld

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.koushikdutta.ion.Ion
import org.json.JSONObject
import uv.tc.packetworld.util.Conexion

class LoginActivity : AppCompatActivity() {

    private lateinit var etNumeroPersonal: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etNumeroPersonal = findViewById(R.id.etNumeroPersonal)
        etContrasena = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnIngresar)

        btnLogin.setOnClickListener {
            val numeroPersonal = etNumeroPersonal.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (numeroPersonal.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            autenticar(numeroPersonal, contrasena)
        }
    }

    private fun autenticar(numeroPersonal: String, contrasena: String) {

        val url = "${Conexion().URL_API}autenticacion/administracion"

        Ion.with(this)
            .load("POST", url)
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .setBodyParameter("numeroPersonal", numeroPersonal)
            .setBodyParameter("contrasena", contrasena)
            .asString()
            .setCallback { e, result ->

                if (e != null) {
                    Toast.makeText(this, "Error de conexión con el servidor: ${e.message}", Toast.LENGTH_LONG).show()
                    return@setCallback
                }

                if (result.isNullOrEmpty()) {
                    Toast.makeText(this, "Respuesta vacía del servidor", Toast.LENGTH_LONG).show()
                    return@setCallback
                }

                try {
                    val json = JSONObject(result)

                    val error = json.getBoolean("error")
                    val mensaje = json.getString("mensaje")

                    if (error) {
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                        return@setCallback
                    }

                    val colaborador = json.getJSONObject("colaborador")
                    val idColaborador = colaborador.getInt("idColaborador")
                    val nombreColaborador = colaborador.optString("nombre", "")

                    Toast.makeText(this, "Bienvenido, $nombreColaborador", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("ID_CONDUCTOR", idColaborador)
                    startActivity(intent)
                    finish()

                } catch (ex: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_LONG).show()
                }
            }
    }
}
