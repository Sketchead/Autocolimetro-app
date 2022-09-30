package mx.tecnm.tepic.autocolimetro_app

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.autocolimetro_app.databinding.ActivityMainBinding
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            //  you will get result here in result.data
            if(result.data!!.data.toString()=="EXCEDE"){
                AlertDialog.Builder(this)
                    .setTitle("ATENCIÓN")
                    .setMessage("Cuenta bloqueada por exceso de alcohol")
                    .show()
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val googleSignOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("612542666031-bmo0ips5p95oolml962p6hv7or7d0sdf.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignOptions)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.signInButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

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
        startForResult.launch(Intent(this,MainActivity2::class.java))
    }
//this,MainActivity2::class.java
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                //Logeado
                val account = accountTask.getResult(ApiException::class.java)
                FirebaseAuthwithGoogle(account)

            }catch(e: Exception){
                AlertDialog.Builder(this)
                    .setMessage("Error onresult"+e.message+e.localizedMessage)
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun FirebaseAuthwithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                //Success
                val firebaseUser = firebaseAuth.currentUser
                val uid = firebaseAuth.uid
                val email = firebaseUser!!.email
                Log.d(TAG,"mail: $email")
                Log.d(TAG,"uid $uid")

                //check if new user
                if(it.additionalUserInfo!!.isNewUser){
                    Log.d(TAG,"Registrado")
                    Toast.makeText(this@MainActivity, "Registrado",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@MainActivity, "Sesión iniciada",Toast.LENGTH_LONG).show()
                }
                val bd = FirebaseFirestore.getInstance()
                val current = Calendar.getInstance()
                val day=current.get(Calendar.DAY_OF_MONTH)
                val month=current.get(Calendar.MONTH)+1
                val year=current.get(Calendar.YEAR)
                var banned = false
                bd.collection(FirebaseAuth.getInstance().uid+"")
                    .get()
                    .addOnSuccessListener { document ->
                        for (documento in document!!){
                            val fecha = documento.getTimestamp("fecha")
                            //val c = Calendar.getInstance()
                            Log.d("TIEMPO SEGS","${fecha!!.seconds}")
                            val test_timestamp = fecha!!.seconds*1000
                            val triggerTime = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(test_timestamp),
                                TimeZone.getDefault().toZoneId()
                            )
                            //triggerTime.dayOfMonth==day && triggerTime.monthValue==(month) && triggerTime.year==year
                            //Toast.makeText(this@MainActivity, "Entra ${triggerTime.dayOfMonth==day && triggerTime.monthValue==(month) && triggerTime.year==year} ",Toast.LENGTH_LONG).show()
                            if(triggerTime.dayOfMonth==day && triggerTime.monthValue==(month) && triggerTime.year==year ){
                                Toast.makeText(this@MainActivity, "baneado",Toast.LENGTH_LONG).show()
                                banned = true
                            }
                        }//for
                        if (banned){
                            AlertDialog.Builder(this)
                                .setTitle("ATENCIÓN")
                                .setMessage("Cuenta bloqueada por exceso de alcohol.\n")
                                .show()
                            FirebaseAuth.getInstance().signOut();
                        }else{
                            invocarOtraVentana()
                        }
                    }
            }
            .addOnFailureListener {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Error en authwiuthgoogle"+it.message+"\n"+it.localizedMessage)
                    .show()
            }
    }

}