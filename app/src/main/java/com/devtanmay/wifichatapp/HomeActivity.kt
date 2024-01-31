package com.devtanmay.wifichatapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    lateinit var sos:CardView
    lateinit var survivalGuideCard:CardView
    lateinit var offChat:CardView
    private val PERMISSIONS_REQUEST_CODE = 123
    private val PERMISSION_REQUEST = 10
    private var permissionEnabled = false
    private val locationPermissionCode = 2
    private lateinit var locationManager: LocationManager
    var latitude:Double?=null
    var longitude:Double?=null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)




        if (checkPermission(permissions)) {
        //    Toast.makeText(applicationContext,"tru",Toast.LENGTH_SHORT).show()
            permissionEnabled = true
        } else {
         //   Toast.makeText(applicationContext,"fasle",Toast.LENGTH_SHORT).show()
            requestPermissions(permissions, PERMISSION_REQUEST)
        }


        sos=findViewById(R.id.idSOScard)
        survivalGuideCard=findViewById(R.id.idSurvivalCard)
        offChat=findViewById(R.id.idChatCard)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) as FusedLocationProviderClient
        sos.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            }

 fusedLocationClient!!.getCurrentLocation(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
        CancellationTokenSource().token)
        .addOnSuccessListener {
            latitude=it.latitude
            longitude=it.longitude

          //  Toast.makeText(this,it.latitude.toString(),Toast.LENGTH_SHORT).show()
            val smsManager: SmsManager = SmsManager.getDefault()
// add the emergency numbers in mobile below
           val mobileNumbers= listOf<String>("")

            for (mobile:String in mobileNumbers){
                try {
                    smsManager.sendTextMessage(mobile, null,
                        "This is sos messsage." +
                                "\n" +
                                "Need help at https://www.google.com/maps/search/?api=1&query=${latitude.toString()}%2C${longitude.toString()}", null, null);
                }
                catch (e:Exception){
                    Toast.makeText(this,"Couldn't send to "+mobile+e.toString(),Toast.LENGTH_SHORT).show()
                }

            }

            Toast.makeText(this,"SOS sending complete",Toast.LENGTH_SHORT).show()
        }
     .addOnFailureListener {
         Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
     }




        }

        offChat.setOnClickListener {


            val intent = Intent(this@HomeActivity, MainActivity::class.java)


            startActivity(intent)

        }



    }




 fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(permissionEnabled)
        {
            if (requestCode == PERMISSION_REQUEST) {
                var allSuccess = true
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allSuccess = false
                        val requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                        if (requestAgain) {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (allSuccess)
                    permissionEnabled = true

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}









