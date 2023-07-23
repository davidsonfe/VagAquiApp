package br.edu.ifpe.recife.tads.vagaqui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import br.edu.ifpe.recife.tads.vagaqui.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double currentLatitude;
    private double currentLongitude;
    private LatLng selectedMarkerPosition; // Store the position of the selected marker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase (if not already done)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Call the method to create the notification channel
        createNotificationChannel();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get data from Firebase and show notifications
        getDataFromFirebase();

        // Verificar se as permissões de localização foram concedidas
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);  // Mostrar o botão para centralizar no local atual

            // Obter a localização atual do dispositivo
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Obter as coordenadas da localização atual
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        // Adicionar um marcador na localização atual
                        LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Minha Localização"));

                        // Configurar o evento de clique no mapa
                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                // Store the selected marker's position
                                selectedMarkerPosition = latLng;
                                // Clear any existing marker
                                mMap.clear();
                                // Add a new marker at the clicked location
                                mMap.addMarker(new MarkerOptions().position(latLng).title("Novo Marcador"));
                                // Exibir o modal de cadastro
                                exibirModalCadastro();
                            }
                        });


                        // Configurar o evento de clique no marcador
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                if (marker.getTitle().equals("Minha Localização") || marker.getTitle().equals("Novo Marcador")) {
                                    // Exibir o modal de cadastro
                                    exibirModalCadastro();
                                }
                                return false;
                            }
                        });

                        // Desenhar um círculo com raio de 200 metros em torno da localização atual
                        CircleOptions circleOptions = new CircleOptions()
                                .center(currentLocation)
                                .radius(200) // raio em metros
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.parseColor("#500000FF")); // cor com transparência
                        mMap.addCircle(circleOptions);

                        // Centralizar o mapa na localização atual
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    }
                }
            });

            // Habilitar controles de zoom
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // Configurar o evento de clique no mapa
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    // Store the selected marker's position
                    selectedMarkerPosition = latLng;
                    // Clear any existing marker
                    mMap.clear();
                    // Add a new marker at the clicked location
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Novo Marcador"));
                    // Exibir o modal de cadastro
                    exibirModalCadastro();
                }
            });

        } else {
            // Caso as permissões de localização não tenham sido concedidas, solicitar permissões
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void exibirModalCadastro() {
        // Inflar o layout do modal
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cadastro, null);

        // Criar o AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();

        // Referenciar os elementos do layout do modal
        EditText editTextString = dialogView.findViewById(R.id.editTextString);
        Button buttonSalvar = dialogView.findViewById(R.id.buttonSalvar);

        // Configurar o evento de clique no botão "Salvar"
        buttonSalvar.setOnClickListener(v -> {
            String texto = editTextString.getText().toString();

            if (texto.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Digite uma Vaga antes de salvar.", Toast.LENGTH_SHORT).show();
            } else {
                // Faça o que for necessário com a string cadastrada e a posição do marcador
                salvarString(texto, selectedMarkerPosition.latitude, selectedMarkerPosition.longitude);
                dialog.dismiss(); // Fechar o modal após salvar
                // Exibir mensagem de sucesso
                Toast.makeText(getApplicationContext(), "Salvando a Vaga...", Toast.LENGTH_SHORT).show();
            }
        });

        // Exibir o modal
        dialog.show();
    }

    private void salvarString(String texto, double latitude, double longitude) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userName = currentUser.getDisplayName(); // Get the user's display name

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference stringsRef = database.child("strings");

            // Generate a new unique key for the parking location
            String key = stringsRef.push().getKey();

            // Create a Map with the data to be saved, including the user's name
            Map<String, Object> parkingData = new HashMap<>();
            parkingData.put("texto", texto);
            parkingData.put("latitude", latitude);
            parkingData.put("longitude", longitude);
            parkingData.put("userName", userName);

            // Save the data to the database using the unique key
            stringsRef.child(key).setValue(parkingData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Local do estacionamento adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Erro ao adicionar o local do estacionamento.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle the case when no user is logged in
            Toast.makeText(getApplicationContext(), "Nenhum usuário está logado. Faça o login para adicionar um local de estacionamento.", Toast.LENGTH_SHORT).show();
        }
    }



    private void getDataFromFirebase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference stringsRef = database.child("strings");

        stringsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String texto = snapshot.child("texto").getValue(String.class);
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);

                    // Create a notification for each data entry
                    showNotification(texto, latitude, longitude);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String texto, double latitude, double longitude) {
        // Create an Intent to open the MapsActivity when the notification is clicked
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("VagAqui -> ")
                .setContentText("Texto: " + texto + ", Latitude: " + latitude + ", Longitude: " + longitude)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(/*Unique notification ID*/ 123, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel Name";
            String description = "Notification Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
