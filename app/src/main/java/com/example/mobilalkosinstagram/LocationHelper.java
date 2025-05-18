package com.example.mobilalkosinstagram;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;


public class LocationHelper {
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission needed", Toast.LENGTH_SHORT).show();
            callback.onLocationReceived(null);
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(location);
                    } else {
                        callback.onLocationReceived(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show();
                    callback.onLocationReceived(null);
                });
    }




    public interface LocationCallback {
        void onLocationReceived(Location location);
    }
}