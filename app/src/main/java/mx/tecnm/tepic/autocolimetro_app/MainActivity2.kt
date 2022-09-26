package mx.tecnm.tepic.autocolimetro_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.doOnTextChanged
import mx.tecnm.tepic.autocolimetro_app.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityMain2Binding
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
            if(ac>4){
                Toast.makeText(this, "Grado de alcohol sobrepasado: "+ac, Toast.LENGTH_LONG).show()

                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(1, builder.build())
                }
            }
        }
    }
}