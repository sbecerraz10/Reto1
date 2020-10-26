package com.example.reto1;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private String user;
    private LocationManager manager;
    private Marker me;
    private TextView distance;
    private Button addHBtn;
    private Button confirmHBtn;
    private Button updateInfo;
    private Geocoder direction;
    private ArrayList<Marker> holes;
    private ArrayList<Marker> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        user = getIntent().getExtras().getString("user");
        distance = findViewById(R.id.distance);
        distance.setVisibility(View.INVISIBLE);
        addHBtn = findViewById(R.id.addHBtn);
        confirmHBtn = findViewById(R.id.confirmHBtn);
        confirmHBtn.setVisibility(View.INVISIBLE);
        confirmHBtn.setClickable(false);
        updateInfo = findViewById(R.id.updateInfo);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        direction = new Geocoder(this, new Locale("es"));
        holes = new ArrayList<Marker>();



    }

    private void burnUsers() {
        users = new ArrayList<Marker>();
        //Icono diferente
        //ICESI
        Marker user = mMap.addMarker(new MarkerOptions().position(new LatLng(3.341937, -76.530058)).title("Usuario 1").icon(BitmapDescriptorFactory.defaultMarker(120)));
        users.add(user);
        //JAVERIANA
        user = mMap.addMarker(new MarkerOptions().position(new LatLng(3.348928, -76.531251)).title("Usuario 2").icon(BitmapDescriptorFactory.defaultMarker(120)));
        users.add(user);
        //LIBRE
        user = mMap.addMarker(new MarkerOptions().position(new LatLng(3.360704, -76.526425)).title("Usuario 3").icon(BitmapDescriptorFactory.defaultMarker(120)));
        users.add(user);
        //Autonoma
        user = mMap.addMarker(new MarkerOptions().position(new LatLng(3.353967, -76.522874)).title("Usuario 4").icon(BitmapDescriptorFactory.defaultMarker(120)));
        users.add(user);
        //Uni Valle
        user = mMap.addMarker(new MarkerOptions().position(new LatLng(3.375005, -76.532673)).title("Usuario 5").icon(BitmapDescriptorFactory.defaultMarker(120)));
        users.add(user);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2,this);

        setInitialPos();
        burnUsers();
        burnHoles();
        addHBtn.setOnClickListener((v)->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alerta de hueco");
            try {
                builder.setMessage("Pulse añadir para alertar un hueco en la posicion con latitud: "
                        + me.getPosition().latitude + " y longitud: " + me.getPosition().longitude +" y direccion: "
                        + direction.getFromLocation(me.getPosition().latitude,me.getPosition().longitude,1).get(0).getAddressLine(0));
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Add the buttons
            builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    holes.add(mMap.addMarker(new MarkerOptions().position
                            ((me.getPosition())).title("Hueco").icon(BitmapDescriptorFactory.fromResource(R.drawable.noconfirm))));
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });


            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });


        confirmHBtn.setOnClickListener((v)->{
            double minDistance = 300;
            double closerHole = Double.POSITIVE_INFINITY;
            Marker thisHole = null;
            int pointer = -1;
            Marker newHole = null;
            for (int i = 0; i < holes.size(); i++) {
                thisHole = holes.get(i);
                double distancemeters = SphericalUtil.computeDistanceBetween(thisHole.getPosition(), me.getPosition());
                if (distancemeters < minDistance && distancemeters < closerHole) {
                    closerHole = distancemeters;
                    pointer = i;
                }
            }
            if(pointer!=-1){
                holes.get(pointer).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.confirmedhole));
                int index = pointer + 1;
                holes.get(pointer).setTitle("Hueco " + index + " confirmado");
            }
        });

        //Boton que actualiza la informacion actual del mapa
        updateInfo.setOnClickListener((v)->{
            //Thread que da la distancia a los huecos
            runOnUiThread(()->{


                if (me != null) {
                    double minDistance = 300;
                    double closerHole = Double.POSITIVE_INFINITY;
                    Marker thisHole = null;
                    for (int i = 0; i < holes.size(); i++) {
                        thisHole = holes.get(i);
                        double distancemeters = SphericalUtil.computeDistanceBetween(thisHole.getPosition(), me.getPosition());
                        if (distancemeters < minDistance && distancemeters < closerHole) {
                            closerHole = distancemeters;
                        }
                    }

                    if (closerHole <= minDistance) {
                        distance.setVisibility(View.VISIBLE);
                        distance.setText("Hueco cercano a " + closerHole + " metros");
                        confirmHBtn.setVisibility(View.VISIBLE);
                        confirmHBtn.setClickable(true);
                    } else {
                        distance.setText("No hay huecos cercanos (300 m)");
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            });
        });





    }

    private void burnHoles() {
        //Hueco 1 no confirmado
        Marker hole = mMap.addMarker(new MarkerOptions().position(new LatLng(3.342432, -76.531014)).title("Hueco 1 no confirmado").icon(BitmapDescriptorFactory.fromResource(R.drawable.noconfirm)));
        holes.add(hole);
        //Hueco 2 no confirmado
        hole = mMap.addMarker(new MarkerOptions().position(new LatLng(3.345807, -76.530667)).title("Hueco 2 no confirmado").icon(BitmapDescriptorFactory.fromResource(R.drawable.noconfirm)));
        holes.add(hole);
        //Hueco 3 no confirmado
        hole = mMap.addMarker(new MarkerOptions().position(new LatLng(3.354248, -76.523408)).title("Hueco 3 no confirmado").icon(BitmapDescriptorFactory.fromResource(R.drawable.noconfirm)));
        holes.add(hole);
        //Hueco 4 confirmado
        hole = mMap.addMarker(new MarkerOptions().position(new LatLng(3.358654, -76.524351)).title("Hueco 4 confirmado").icon(BitmapDescriptorFactory.fromResource(R.drawable.confirmedhole)));
        holes.add(hole);
        //Hueco 5 confirmado
        hole = mMap.addMarker(new MarkerOptions().position(new LatLng(3.370478, -76.529445)).title("Hueco 5 confirmado").icon(BitmapDescriptorFactory.fromResource(R.drawable.confirmedhole)));
        holes.add(hole);

    }

    @SuppressLint("MissingPermission")
    public void setInitialPos(){
     Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      if(location!=null) {
          updateMyLocation(location);
      }

    }

    @Override
    public void onLocationChanged(Location location) {
        updateMyLocation(location);
    }

    public void updateMyLocation(Location location){
        LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(me==null){
            me = mMap.addMarker(new MarkerOptions().position(myPos).title("Me"));
        }else{
            me.setPosition(myPos);
        }


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos,17));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}