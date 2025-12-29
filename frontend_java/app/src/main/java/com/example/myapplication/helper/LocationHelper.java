package com.example.myapplication.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Helper class for handling location-related operations
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationResultListener listener;

    public interface LocationResultListener {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permissions
     */
    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Check if location services are enabled
     */
    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Open location settings
     */
    public void openLocationSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivity(intent);
    }

    /**
     * Get current location (one-time)
     */
    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationResultListener listener) {
        this.listener = listener;

        if (!hasLocationPermission()) {
            listener.onLocationError("Chưa cấp quyền truy cập vị trí");
            return;
        }

        if (!isLocationEnabled()) {
            listener.onLocationError("Vui lòng bật dịch vụ vị trí");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        // Request new location if last location is null
                        requestNewLocation(listener);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location: " + e.getMessage());
                    listener.onLocationError("Không thể lấy vị trí: " + e.getMessage());
                });
    }

    /**
     * Request new location update
     */
    @SuppressLint("MissingPermission")
    private void requestNewLocation(LocationResultListener listener) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else {
                    listener.onLocationError("Không thể lấy vị trí hiện tại");
                }
                stopLocationUpdates();
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @return distance in kilometers
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Format distance for display
     * @param distanceInKm distance in kilometers
     * @return formatted string like "5.2 km" or "800 m"
     */
    public static String formatDistance(double distanceInKm) {
        if (distanceInKm < 1) {
            return String.format(java.util.Locale.US, "%.0f m", distanceInKm * 1000);
        } else {
            return String.format(java.util.Locale.US, "%.1f km", distanceInKm);
        }
    }

    /**
     * Open Google Maps or Goong Maps for navigation
     */
    public void openNavigationApp(Activity activity, double destLat, double destLng, String destName) {
        // Try to open Google Maps first
        // IMPORTANT: Use Locale.US to ensure dot (.) decimal separator, not comma (,)
        Uri gmmIntentUri = Uri.parse(String.format(java.util.Locale.US,
                "google.navigation:q=%f,%f&mode=d", destLat, destLng));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(mapIntent);
        } else {
            // Fallback to browser with Google Maps
            Uri browserUri = Uri.parse(String.format(java.util.Locale.US,
                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=driving",
                    destLat, destLng));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
            activity.startActivity(browserIntent);
        }
    }

    /**
     * Handle permission result
     * @return true if permission granted, false otherwise
     */
    public static boolean handlePermissionResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}

