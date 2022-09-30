package mx.tecnm.tepic.autocolimetro_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.autocolimetro_app.databinding.ActivityMain2Binding
import java.sql.Timestamp
import java.time.LocalDate


class MainActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityMain2Binding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "nc"
            val descriptionText ="Auto"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("1", name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        var builder = NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.drawable.carrito)
            .setContentTitle("Alcolimetro")
            .setContentText("Límite de alcohol superado")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        binding.gradoAl.doOnTextChanged { text, start, before, count ->
            if(text!!.isEmpty()) return@doOnTextChanged
            val ac = text.toString().toFloat()
            if(ac>=4){
                Toast.makeText(this, "Grado de alcohol sobrepasado: "+ac, Toast.LENGTH_LONG).show()

                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(1, builder.build())
                    val current = LocalDate.now()
                    val year=current.year
                    val month=current.month.value
                    val day=current.dayOfMonth
                    val datos = hashMapOf(
                        "fecha" to Timestamp((year - 1900), (month - 1), (day), 0, 0, 0, 0),
                    )
                    val bd = FirebaseFirestore.getInstance()
                    bd.collection(FirebaseAuth.getInstance().uid+"")
                        .add(datos)
                        .addOnSuccessListener {
                            Log.d("REPORTE","Insertado")
                        }
                        .addOnFailureListener {
                            AlertDialog.Builder(this@MainActivity2)
                                .setTitle("Error")
                                .setMessage(it.message)
                                .show()
                        }
                }
                val data = Intent()
                data.setData(Uri.parse("EXCEDE"));
                setResult(RESULT_OK, data);
                FirebaseAuth.getInstance().signOut();
                finish()
            }
        }
    }
}