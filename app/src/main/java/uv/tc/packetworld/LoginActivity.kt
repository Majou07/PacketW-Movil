package uv.tc.packetworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle){
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnIngresar.setOnClickListener {
            verificarCrenciales()
        }
    }

    fun verificarCrenciales(){
        if (sonCamposValidos()){
            consumirAPI(binding.etMatricula.text.toString(),
                binding.etPassword.text.toString())
        }
    }

    fun sonCamposValidos(): Boolean{
        var valido = true
        if (binding.etMatricula.text.isEmpty()){
            binding.etMatricula.setError("Matricula obligatoria")
            valido = false
        }

        if (binding.etPassword.text.isEmpty()){
            binding.etPassword.setError("Contraseña obligatoria")
            valido = false
        }
        return valido;
    }
}