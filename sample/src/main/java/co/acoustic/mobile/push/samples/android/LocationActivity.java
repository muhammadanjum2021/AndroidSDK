/*
 * Copyright © 2011, 2019 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any intellectual or
 * industrial property rights of Acoustic, L.P. except as may be provided in an agreement with
 * Acoustic, L.P. Any unauthorized copying or distribution of content from this file is
 * prohibited.
 */
package co.acoustic.mobile.push.samples.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import co.acoustic.mobile.push.sdk.location.LocationManager;
import co.acoustic.mobile.push.sdk.location.LocationPreferences;
import co.acoustic.mobile.push.sdk.location.LocationApi;

import java.util.LinkedList;
import java.util.List;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener{

    private static final int REQUEST_LOCATION = 0;

    private static final String[] LOCATION_PERMISSIONS = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};

    private GoogleMap mMap;
    private Button enableLocations;
    private Button showGeofences;
    private boolean mapEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        enableLocations = (Button)findViewById(R.id.enableLocations);
        showGeofences = (Button)findViewById(R.id.showGeofences);
        boolean locationsEnabled = LocationPreferences.isEnableLocations(getApplicationContext());
        enableLocations.setText(locationsEnabled ? getResources().getString(R.string.disable_locations_text) : getResources().getString(R.string.enable_locations_text));
        showGeofences.setEnabled(false);

        enableLocations.setOnClickListener(this);
        showGeofences.setOnClickListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        try {
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            Log.e("Location", "Failed to start map", e);
        }

    }

    @Override
    public void onClick(View view) {
        if(enableLocations == view) {
            boolean locationsEnabled = LocationPreferences.isEnableLocations(getApplicationContext());
            if(locationsEnabled) {
                LocationManager.disableLocationSupport(getApplicationContext());
                enableLocations.setText(getResources().getString(R.string.enable_locations_text));
                showGeofences.setEnabled(false);
            } else {
                if (!checkSelfPermissions()) {
                    ActivityCompat.requestPermissions(this,
                            getPermissions(true), REQUEST_LOCATION);

                } else {
                    LocationManager.enableLocationSupport(getApplicationContext());
                    enableLocations.setText(getResources().getString(R.string.disable_locations_text));
                    showGeofences.setEnabled(mapEnabled);
                }

            }
        } else if(showGeofences == view) {
            mMap.clear();
            List<LocationApi> locations = LocationManager.getAllLocations(getApplicationContext());

            for(LocationApi location : locations) {
                LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(location.getRadius())
                        .strokeColor(Color.RED)
                        .strokeWidth(1)
                        .fillColor(0x5500ff00));
                mMap.addMarker(new MarkerOptions().position(center).title(location.getId()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }

    private String[] getPermissions(boolean forRequest) {
        List<String> permissions = new LinkedList<>();
        for(String permission : LOCATION_PERMISSIONS) {
            permissions.add(permission);
        }
        if(forRequest) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        return permissions.toArray(new String[permissions.size()]);

    }

    private boolean checkSelfPermissions() {
        String[] permissions = getPermissions(false);
        for(String permission : permissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mapEnabled = true;
        boolean locationsEnabled = LocationPreferences.isEnableLocations(getApplicationContext());
        showGeofences.setEnabled(locationsEnabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationManager.enableLocationSupport(getApplicationContext());
                showGeofences.setEnabled(true);
                enableLocations.setText(getResources().getString(R.string.disable_locations_text));
                showGeofences.setEnabled(mapEnabled);
            }
        }
    }


}
