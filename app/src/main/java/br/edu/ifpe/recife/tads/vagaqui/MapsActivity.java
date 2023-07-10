package br.edu.ifpe.recife.tads.vagaqui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import br.edu.ifpe.recife.tads.vagaqui.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Adicionar um marcador na localização atual
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Minha Localização"));

                        // Adicionar um marcador na localização atual
                        currentLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Minha Localização"));

                        // Configurar o evento de clique no marcador
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                if (marker.getTitle().equals("Minha Localização")) {
                                    // Exibir o modal de cadastro
                                    exibirModalCadastro();
                                }
                                return false;
                            }
                        });

                        // Desenhar um círculo com raio de 500 metros em torno da localização atual
                        CircleOptions circleOptions = new CircleOptions()
                                .center(currentLocation)
                                .radius(500) // raio em metros
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.parseColor("#500000FF")); // cor com transparência
                        mMap.addCircle(circleOptions);
                    }
                }
            });

            // Habilitar controles de zoom
            mMap.getUiSettings().setZoomControlsEnabled(true);

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

            if(texto.isEmpty()){
                Toast.makeText(getApplicationContext(), "Digite uma Vaga antes de salvar.", Toast.LENGTH_SHORT).show();
            }else {
                // Faça o que for necessário com a string cadastrada
                salvarString(texto);
                dialog.dismiss(); // Fechar o modal após salvar
                // Exibir mensagem de sucesso
                Toast.makeText(getApplicationContext(), "Salvando a Vaga...", Toast.LENGTH_SHORT).show();
            }
        });

        // Exibir o modal
        dialog.show();
    }



    private void salvarString(String texto) {
        // Obter uma referência para o nó "strings" no Firebase Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference stringsRef = database.child("strings");

        // Gerar uma nova chave única para a string
        String key = stringsRef.push().getKey();

        // Criar um objeto Map com os dados a serem salvos
        Map<String, Object> stringValues = new HashMap<>();
        stringValues.put("texto", texto);

        // Salvar os dados no banco de dados
        stringsRef.child(key).setValue(stringValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Vaga salva com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro ao salvar a Vaga.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }


}
