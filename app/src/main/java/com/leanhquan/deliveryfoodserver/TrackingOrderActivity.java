package com.leanhquan.deliveryfoodserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.leanhquan.deliveryfoodserver.API.IGeoCoordinates;
import com.leanhquan.deliveryfoodserver.Common.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //fist of all you must be check perrmision and check play service

    //if you appliocation want high acuracy location so must declare Location request

    private final static String     DEBUG = "DEBUG"; //for log
    private final static int        PLAY_SERVICE_RESOLUTION_REQUEST = 1000; //request for slove solution
    private final static int        LOCATION_PERMISSION_REQUEST = 1001;   //requset for permission to get Location
    private static int              UPDATE_INTERVAL = 1000; //Set the desired interval for active location updates, in milliseconds.  (min for maping)
    private static int              FAST_INTERVAL = 5000;  //appropriate for mapping applications that are showing your location in real-time.  (max for maping)
    private static int              DISPLACEMENT = 10;     //Set the minimum displacement between location updates in meters - 10m

    private Location mLastLocation;               //A data class representing a geographic location. must be have longtitude, latitude and timeslap
    private GoogleApiClient mGoogleApiClient;     // object to access the Google APIs provided in the Google Play services library
    private LocationRequest mLocationRequest;    //objects are used to request a quality of service for location updates from the FusedLocationProviderApi.
    private IGeoCoordinates mIGeoService;

    private GoogleMap mMap;     //map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        mIGeoService = Common.getIGeoService();

        //always check permission location FIRST if you wanna connect to GoogleAPI and create location request for maping
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
         && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            RequestRuntimePermission();//if all permission granted start connect to GoogleAPI and create request


        } else {  //when permission already grated before application start
            if (checkPlayService()) { //SECOND step is check playService is available
                buildGoogleAPIClinet(this); //connect to GoogleAPI
                createLocationRequest(); //create a request to get your location
            }
        }

        displayLocation(); // call it when all permission granted and already have a location to mapping to map


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); //init map
        mapFragment.getMapAsync(this); //callback map is already
    }

    protected synchronized void buildGoogleAPIClinet(TrackingOrderActivity trackingOrderActivity) {  //must be synchornized for safe
        mGoogleApiClient = new GoogleApiClient.Builder(this)  //declare GoogleAIP
                //todo:FIX BUG cannot be cast to com.google.android.gms.common.api.GoogleApiClient$ConnectionCallbacks
                .addConnectionCallbacks(this) // callback of connection of GoogleAPIClient and return this fragment
                .addOnConnectionFailedListener(this) //listener callback of connection and return
                .addApi(LocationServices.API).build(); //Register API is LocationService to use location Service to use FusedLocationApi to connect LocationProvider to get Location
        mGoogleApiClient.connect(); // connect

    } //connect to GoogleAPI and if you want to get location you must be register LocationService to get Location


    private void RequestRuntimePermission() {
        ActivityCompat.requestPermissions(
                this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION_REQUEST
        );
    }    //push request code for activitty when all permission granted

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:  //if it's request code
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){  //FIRST permission is granted
                    if (checkPlayService()) { //SECOND check playService
                        buildGoogleAPIClinet(this);  //connect to GoogleAPI
                        createLocationRequest();  //create a request to get your location
                        displayLocation();       //mapping it onto map
                    }
                }
            break;
        }
    }  //recive request code for permission

    private void displayLocation() {
        //first off all check perrmission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            RequestRuntimePermission();  //if your application not granted permission -> push request to connect permission
        } else {

            //already granted
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);  //FusedLocationApi: method interacing to location providers
                                                                                                                //  and must be used in conjuction with GoogleAPI Client

            if (mLastLocation != null){  //if have last location
                double latitude = mLastLocation.getLatitude();  //get latitude of this location
                double longitude  = mLastLocation.getLongitude(); //get latitude of this location

                //add Markers for your location and move camera
                LatLng yourLocation = new LatLng(latitude, longitude); //location with kinh độ và vĩ độ
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your location")); //put marker in position - location already get
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));  //move camera to location already get
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));  //and zoom to 17

                //after Add marker for your location, add marker for this order and draw route
                drawRoute(yourLocation, Common.currentRequest.getAddress());
            } else {
                //IF DONT HAVE LOCATION
                Toast.makeText(this, "Could not get your location", Toast.LENGTH_SHORT).show();
            }
        }
    } //if permission not granted -> go to check, if its granted -> getLocation via LocationService.FusedLocationApi and put onto map

    private void drawRoute(LatLng yourLocation, String address) {
        mIGeoService.getGeoCode(address, "AIzaSyBwDi9OBsWbdpSoaZFhoLJpF8_L7LYxNkI").enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response.body()));

                    Log.d(DEBUG, "onResponse: "+ jsonObject);

                    String lat = ((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lat").toString();

                    String lon = ((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();

                    LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                    Log.d(DEBUG, "onResponse: Order location "+ orderLocation);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_box);
                    bitmap = Common.scaleBitmap(bitmap, 70, 70);

                    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .title("Order of "+Common.currentRequest.getPhone())
                            .position(orderLocation);

                    mMap.addMarker(markerOptions);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();  //create request for location
        mLocationRequest.setInterval(UPDATE_INTERVAL); // interval for location update (min)
        mLocationRequest.setFastestInterval(FAST_INTERVAL); //interval for showing location in real-time
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Priority : HIGH ACCURACY - Độ chính xác cao
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);  //minimum displacement between location updates - 10m
    }  //create location request when connected to GoogleAPI service

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this); //resultCode = isAvailable
        if (resultCode != ConnectionResult.SUCCESS) { //if not available
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){  //if not available because an error
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show(); //show dialog for user with handle solution
            } else {
                //and if for no reason the error show
                Toast.makeText(this, "This device no support", Toast.LENGTH_SHORT).show();
                finish(); //finish activity
            }
        }
        return true; // If GoogleApiAvailability.isGooglePlayServicesAvailable(Context) returns true, GoogleApi instances will not show any UI to resolve connection failures.

    }   //check GoogleAPI playService available

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //LatLng sydney = new LatLng(-34, 151);  // declare position
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney")); //put marker onto position
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney)); //move camera to position
    } //set up map when started with location

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location; //set location changed = lastLocation
        displayLocation();  //display location has been changed
    } //when location change -> listener

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
    } //when Google API connected -> check permission and get location AGAIN

    private void startLocationUpdate() {
        //check permission when location update
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; //if not granted -> finish
        }
        //if it granted
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        //LocationServiceAPI request location update via (Google API Client, Request again, listener Location uopdate)
    } // location update -> request again and listener this location

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    } //when connection Google API suspended -> Google API connect again

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    } //when connection Google API fail -> return

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayService();
    } // when app resume -> check Service for handle

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    } // when app start -> if have a Google API so connect

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
