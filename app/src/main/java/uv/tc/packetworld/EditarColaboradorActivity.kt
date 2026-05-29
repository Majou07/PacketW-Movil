package uv.tc.packetworld

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.packetworld.databinding.ActivityEditarColaboradorBinding
import uv.tc.packetworld.dto.Respuesta
import uv.tc.packetworld.pojo.Colaborador
import uv.tc.packetworld.util.Conexion
import java.io.ByteArrayOutputStream

class EditarColaboradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarColaboradorBinding
    private lateinit var conductor: Colaborador
    private var idConductor = -1
    private var fotoSeleccionada: ByteArray? = null

    // Expresiones regulares equivalentes a las del cliente escritorio
    private val REGEX_CURP = "^[A-Z0-9]{18}$"
    private val REGEX_CORREO = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    private val REGEX_NUMERO_PERSONAL = "^EMP[0-9]{3}$"
    private val REGEX_NUMERO_LICENCIA = "^LIC[0-9]{6}$"

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
        obtenerFoto()

        binding.btnActualizar.setOnClickListener {
            if (validarCampos()) {
                actualizarDatos()
                fotoSeleccionada?.let {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    subirFoto(bitmap)
                }
            }
        }

        binding.btnRegresar.setOnClickListener { finish() }

        binding.ivFotografia.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            binding.ivFotografia.setImageBitmap(bitmap)

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            fotoSeleccionada = stream.toByteArray()
        }
    }

    private fun cargarDatos() {
        val url = "${Conexion().URL_API}colaborador/obtener/$idConductor"

        Ion.with(this)
            .load(url)
            .setHeader("Accept-Charset", "UTF-8")
            .asString(Charsets.UTF_8)
            .setCallback { e, result ->
                if (e != null || result.isNullOrEmpty()) {
                    Toast.makeText(this, "API no disponible, no se pueden cargar datos", Toast.LENGTH_LONG).show()
                    binding.btnActualizar.isEnabled = false
                    return@setCallback
                }

                try {
                    conductor = Gson().fromJson(result.trim(), Colaborador::class.java)

                    binding.etNombre.setText(conductor.nombre)
                    binding.etApellidoPaterno.setText(conductor.apellidoPaterno)
                    binding.etApellidoMaterno.setText(conductor.apellidoMaterno)
                    binding.etCorreo.setText(conductor.correoElectronico)
                    binding.etCurp.setText(conductor.curp)
                    binding.etContrasena.setText(
                        conductor.contrasena?.let { "*".repeat(it.length) } ?: ""
                    )
                    binding.etNumeroLicencia.setText(conductor.numeroLicencia)

                } catch (ex: Exception) {
                    Log.e("API", "Error al parsear datos", ex)
                }
            }
    }

    private fun obtenerFoto() {
        val url = "${Conexion().URL_API}colaborador/obtener-foto/$idConductor"

        Ion.with(this)
            .load(url)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val colaborador = Gson().fromJson(result, Colaborador::class.java)
                        colaborador?.fotografia?.let { base64 ->
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            binding.ivFotografia.setImageBitmap(bitmap)
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(this, "Error al procesar foto", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Error al cargar foto", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun subirFoto(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val bytes = stream.toByteArray()

        Ion.with(this)
            .load("PUT", "${Conexion().URL_API}colaborador/subir-foto/$idConductor")
            .setHeader("Content-Type", "application/octet-stream")
            .setByteArrayBody(bytes)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val respuesta = Gson().fromJson(result, Respuesta::class.java)
                        Toast.makeText(this, respuesta.mensaje, Toast.LENGTH_LONG).show()
                    } catch (ex: Exception) {
                        Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Error al subir foto", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun actualizarDatos() {
        conductor.nombre = binding.etNombre.text.toString()
        conductor.apellidoPaterno = binding.etApellidoPaterno.text.toString()
        conductor.apellidoMaterno = binding.etApellidoMaterno.text.toString()
        conductor.correoElectronico = binding.etCorreo.text.toString()
        conductor.curp = binding.etCurp.text.toString()
        conductor.contrasena = binding.etContrasena.text.toString()
        conductor.numeroLicencia = binding.etNumeroLicencia.text.toString()

        fotoSeleccionada?.let {
            conductor.fotografia = Base64.encodeToString(it, Base64.DEFAULT)
        }

        val jsonBody = Gson().toJson(conductor)

        Ion.with(this)
            .load("PUT", "${Conexion().URL_API}colaborador/editar")
            .setHeader("Content-Type", "application/json")
            .setStringBody(jsonBody)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val r = Gson().fromJson(result, Respuesta::class.java)
                        Toast.makeText(this, r.mensaje, Toast.LENGTH_LONG).show()
                        if (!r.error) finish()
                    } catch (ex: Exception) {
                        Log.e("API", "Error al parsear respuesta", ex)
                        Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validarCampos(): Boolean {
        var valido = true

        if (binding.etNombre.text.isNullOrBlank()) {
            binding.etNombre.error = "Campo obligatorio"
            valido = false
        }

        if (binding.etApellidoPaterno.text.isNullOrBlank()) {
            binding.etApellidoPaterno.error = "Campo obligatorio"
            valido = false
        }

        if (!binding.etCurp.text.toString().matches(REGEX_CURP.toRegex())) {
            binding.etCurp.error = "CURP inválida"
            valido = false
        }

        if (!binding.etCorreo.text.toString().matches(REGEX_CORREO.toRegex())) {
            binding.etCorreo.error = "Correo inválido"
            valido = false
        }

        if (binding.etContrasena.text.length < 8) {
            binding.etContrasena.error = "Mínimo 8 caracteres"
            valido = false
        }

        if (binding.etNumeroLicencia.isEnabled &&
            !binding.etNumeroLicencia.text.toString().matches(REGEX_NUMERO_LICENCIA.toRegex())) {
            binding.etNumeroLicencia.error = "Formato inválido"
            valido = false
        }

        return valido
    }
}
