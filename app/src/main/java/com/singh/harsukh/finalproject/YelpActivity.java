package com.singh.harsukh.finalproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.singh.harsukh.yelpapilibrary.Yelp;

public class YelpActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Yelp mYelp;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (google_serv_check()) {
            setContentView(R.layout.activity_yelp);
            initMaps();
            mYelp = new Yelp(getResources().getString(R.string.yelp_consumer_key),getResources().getString(R.string.yelp_consumer_secret),getResources().getString(R.string.yelp_token),getResources().getString(R.string.yelp_token_secret));
        }
    }

    private void initMaps() //setting the map fragment and loading it
    {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.MapFragment); //find the fragment by id
        mapFragment.getMapAsync(this); //loads maps asynchronously then calls the onMapsReady
    }

    public boolean google_serv_check() //checks if service is available
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        if (apiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = apiAvailability.getErrorDialog(YelpActivity.this, isAvailable, 0);
            dialog.show();
        }
        return false;
    }

    public static Intent getActivity(Context context)
    {
        Intent activity_intent = new Intent(context, YelpActivity.class);
        return activity_intent;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if(mGoogleMap != null)
        {
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
//                    YelpActivity.this.setMarker("Local", latLng.latitude, latLng.longitude);
                }
            });
            //custom window designed in here too
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null); //retrieves the info_window xml layout
                    //the root in the function is set to null because no root is needed
                    TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tv_snippet);

                    //setting the textViews  use the marker to retrieve this data
                    LatLng ll = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + ll.latitude);
                    tvLng.setText("Longitude: " + ll.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });
        }
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API) //adds the api for location services
                .addConnectionCallbacks(this) // callback method for connection
                .addOnConnectionFailedListener(this) //checks if connection failed
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create(); //creates location request object
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //sets priority of request with high location accuract
        mLocationRequest.setInterval(1000); //location retrieved every second
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); //updates location and listens for the location
        //calls  onLocationChanged();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) //gets location based on updated position
    {
        if(location == null)
        {
            Toast.makeText(getApplicationContext(), "Can't get location", Toast.LENGTH_LONG).show();
        }
        else
        {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mGoogleMap.animateCamera(update);
        }
    }
}
