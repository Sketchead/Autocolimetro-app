package mx.tecnm.tepic.autocolimetro_app

import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.tepic.autocolimetro_app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Ya hay user iniciado
        //val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account)
        //}binding.signInButton.setSize(SignInButton.SIZE_STANDARD)
        binding.register.setOnClickListener {
            if(binding.mail.text.isEmpty() || binding.pass.text.isEmpty()){
                Toast.makeText(this, "Favor de llenar los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(binding.pass.text.length<8){
                Toast.makeText(this, "La contraseña debe ser de al menos 8 carácteres", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val autenticacion = FirebaseAuth.getInstance()
            val dialogo = ProgressDialog(this)
            dialogo.setMessage("Creando usuario")
            dialogo.setCancelable(false)
            dialogo.show()

            autenticacion.createUserWithEmailAndPassword(binding.mail.text.toString(),
                binding.pass.text.toString())
                .addOnCompleteListener {
                    dialogo.dismiss()
                    if(it.isSuccessful) {
                        Toast.makeText(this, "Registrado", Toast.LENGTH_LONG).show()
                        binding.pass.text.clear()
                        binding.mail.text.clear()
                    }else{
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("No se pudo inscribir")
                            .show()
                    }
                }
        }
        binding.signin.setOnClickListener {
            if(binding.mail.text.isEmpty() || binding.pass.text.isEmpty()){
                Toast.makeText(this, "Favor de llenar los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val autenticacion = FirebaseAuth.getInstance()
            val dialogo = ProgressDialog(this)
            dialogo.setMessage("Iniciando sesión")
            dialogo.setCancelable(false)
            dialogo.show()

            autenticacion.signInWithEmailAndPassword(binding.mail.text.toString(),binding.pass.text.toString())
                .addOnCompleteListener {
                    dialogo.dismiss()
                    if(it.isSuccessful){
                        invocarOtraVentana()
                        return@addOnCompleteListener
                    }else {
                        AlertDialog.Builder(this)
                            .setMessage("Error! Correo/Contraseña no validos")
                            .show()
                    }

                }
        }

    }
    private fun invocarOtraVentana() {
        startActivity(Intent(this,MainActivity2::class.java))
        finish()
    }
}