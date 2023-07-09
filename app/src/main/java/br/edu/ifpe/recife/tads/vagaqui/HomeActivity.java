package br.edu.ifpe.recife.tads.vagaqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private br.edu.ifpe.recife.tads.vagaqui.FirebaseAuthListener authListener;
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        this.fbAuth = FirebaseAuth.getInstance();
        this.authListener = new br.edu.ifpe.recife.tads.vagaqui.FirebaseAuthListener(this);
    }


    public void buttonSignOutClick(View view) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.signOut();
        } else {
            Toast.makeText(HomeActivity.this, "Erro!", Toast.LENGTH_SHORT).show();
        }
    }

    public void buttonAbrirMapaClick(View view) {
        Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        fbAuth.addAuthStateListener(authListener); }
    @Override
    public void onStop() {
        super.onStop();
        fbAuth.removeAuthStateListener(authListener); }

}