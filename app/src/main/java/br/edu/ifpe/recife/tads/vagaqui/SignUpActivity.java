package br.edu.ifpe.recife.tads.vagaqui;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText edEmail;

    private EditText edPassword;
    private FirebaseAuth fbAuth;
    private br.edu.ifpe.recife.tads.vagaqui.FirebaseAuthListener authListener;
    private Message edName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edEmail = findViewById(R.id.edit_email);
        edPassword = findViewById(R.id.edit_password);

        this.fbAuth = FirebaseAuth.getInstance();
        this.authListener = new br.edu.ifpe.recife.tads.vagaqui.FirebaseAuthListener(this);

    }



    public void buttonSignUpClick(View view) {
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();

        // Verifica se os campos estão vazios
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }


        FirebaseAuth mAuth = FirebaseAuth.getInstance(); mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    String msg = task.isSuccessful() ? "SIGN UP OK!":
                            "SIGN UP ERROR!"; Toast.makeText(SignUpActivity.this, msg,
                            Toast.LENGTH_SHORT).show();
                });

    }

    public void buttonSignInClick(View view) {
        String login = edEmail.getText().toString();
        String passwd = edPassword.getText().toString();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(login, passwd)
                .addOnCompleteListener(this, task -> {
                    String msg = task.isSuccessful() ? "SIGN IN OK!": "SIGN IN ERROR!";
                    Toast.makeText(SignUpActivity.this, msg,
                            Toast.LENGTH_SHORT).show();
                });
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