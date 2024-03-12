package com.example.mobcomp.ui.theme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.mobcomp.AppDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
@Composable
fun MapScreen(
    onNavigateToInfo: () -> Unit,
    database: AppDatabase,
    applicationContext: Context
) {
    var latitude by remember { mutableStateOf("65.01221") }
    var longitude by remember { mutableStateOf("25.46164") }
    var location by remember { mutableStateOf(LatLng(latitude.toDouble(), longitude.toDouble())) }
    var cameraPosition by remember { mutableStateOf(CameraPosition.fromLatLngZoom(location, 10f)) }
    val cameraPositionState = rememberCameraPositionState { position = cameraPosition }
    var locationText by remember { mutableStateOf("Requesting location...") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.width(120.dp)
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.width(120.dp)
            )
        }

        Button(onClick = {
            location = LatLng(latitude.toDouble(), longitude.toDouble())
            cameraPosition = CameraPosition.fromLatLngZoom(location, 10f)
            cameraPositionState.position = cameraPosition
        }) {
            Text(text = "Show location")
        }
        LaunchedEffect(key1 = Unit) {
            currentLocation(applicationContext,
                onSuccess = { loc ->
                    location = LatLng(loc!!.latitude, loc.longitude)
                    cameraPosition = CameraPosition.fromLatLngZoom(location, 10f)
                    cameraPositionState.position = cameraPosition
                    locationText = "Lat: ${loc?.latitude}, Lon: ${loc?.longitude}"
                },
                onFailure = { exception ->
                    locationText = "Failed to get location: ${exception.message}"
                }
            )
        }

        Text(text = locationText)
        GoogleMap(
            modifier = Modifier.width(400.dp).height(400.dp),
            cameraPositionState = cameraPositionState
        )

        Button(onClick = { onNavigateToInfo() }) {
            Text(text = "Back to Info screen")
        }
    }
}
fun currentLocation(applicationContext: Context, onSuccess: (Location?) -> Unit, onFailure: (Exception) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    val cancellationTokenSource = CancellationTokenSource()
    if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        return
    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
        .addOnSuccessListener { location: Location? ->
            onSuccess(location)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}