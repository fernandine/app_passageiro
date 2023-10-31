package mobi.audax.tupi.passageiro.activities.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.apache.commons.lang3.StringUtils;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.cadastro.CadastroActivity;
import mobi.audax.tupi.passageiro.activities.home.HomeActivity;
import mobi.audax.tupi.passageiro.activities.recuperar_senha.RecuperarSenhaActivity;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.controller.PassageiroController;
import mobi.audax.tupi.passageiro.bin.dto.GoogleAuth;
import mobi.audax.tupi.passageiro.bin.dto.Login;
import mobi.audax.tupi.passageiro.bin.task.location.LocationCommons;
import mobi.audax.tupi.passageiro.bin.task.location.LocationThread;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private final int RC_SIGN_IN = 777;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser user;

    private TextInputLayout email;
    private TextInputLayout senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bem_vindo_login);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        var locationCommons = new LocationCommons();
        if (locationCommons.isLocationEnabled(this)) {
            new LocationThread(this, location -> null).requestLocation();
        } else {
            Util.showAlertDialog(
                    getString(R.string.atencao),
                    getString(R.string.a_partir_desse_ponto_sua_localizacao_deve_esta_habilitada),
                    this,
                    () -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            );
        }
    }

    private void createGoogleRequest() {
        var gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initViews() {
        mAuth = FirebaseAuth.getInstance();
        createGoogleRequest();

        email = this.findViewById(R.id.editText_Login);
        senha = this.findViewById(R.id.editText_Password);

        findViewById(R.id.BtnCriarConta).setOnClickListener(view -> {
            var intent = new Intent(this, CadastroActivity.class);
            intent.putExtra("google", false);
            startActivity(intent);
        });
    }

    public void onGoogleLogin(View view) {
        mGoogleSignInClient.signOut();
        signIn();
    }

    public void onNormalLogin(View view) {
        if (formOk()) {
            login();
        }
    }

    public boolean formOk() {
        var ok = true;
        var inputs = new TextInputLayout[]{email, senha};

        for (var input : inputs) {
            input.setErrorEnabled(false);
            if (StringUtils.isBlank(input.getEditText().getText().toString())) {
                input.setError(getText(R.string.campo_obrigatorio));
                input.setErrorEnabled(true);
                ok = false;
                break;
            }
        }

        return ok;
    }

    private void signIn() {
        var signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            var task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                var account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        var credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        user = mAuth.getCurrentUser();

                        var passenger = new Passenger();
                        passenger.setFireBaseId(user.getUid());
                        passenger.getPessoa().setNome(user.getDisplayName());
                        passenger.setEmail(user.getEmail());
                        passenger.getPessoa().getFotoPerfil().setCaminho(user.getPhotoUrl().toString());
                        loginGoogle(passenger);
                    } else {
                        Toast.makeText(this, R.string.voce_ainda_nao_possui_cadastro_tupi, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void login() {
        var user = email.getEditText().getText().toString();
        var pass = senha.getEditText().getText().toString();
        var token = new Prefs(this).getToken();

        var controller = new PassageiroController(this);
        controller.login(new Login(user, pass, token), () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.acessando), true);
            return null;
        }, statusCode -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (statusCode) {
                case 200 -> {
                    Intent returnIntent = new Intent();
                    Passenger autenticado = new PassengerBo(this).autenticado();
                    if (autenticado != null) {
                        returnIntent.putExtra("isLoggedIn", true);
                        setResult(Activity.RESULT_OK, returnIntent);
                    } else {
                        returnIntent.putExtra("isLoggedIn", false);
                        setResult(Activity.RESULT_OK, returnIntent);
                    }
                    finish();
                }
                case 401 -> {
                    senha.setErrorEnabled(true);
                    senha.setError(getString(R.string.senha_nao_confere_login));
                }
                case 412 -> {
                    email.setErrorEnabled(true);
                    email.setError(getString(R.string.passageiro_nao_encontrado));
                }
                case 999 ->
                        Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });

    }

    private void loginGoogle(@NonNull Passenger passenger) {
        var fireBaseId = passenger.getFireBaseId();
        var token = new Prefs(this).getToken();

        var controller = new PassageiroController(this);
        controller.googleLogin(new GoogleAuth(fireBaseId, token), () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.acessando), true);
            return null;
        }, statusCode -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (statusCode) {
                case 200 -> {
                    finish();
                    startActivity(new Intent(this, HomeActivity.class));
                }
                case 428 -> {
                    var intent = new Intent(this, CadastroActivity.class);
                    intent.putExtra(Passenger.class.getSimpleName(), passenger);
                    intent.putExtra("google", true);
                    startActivity(intent);
                }
                case 999 ->
                        Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    public void onRecuperarSenha(View view) {
        startActivity(new Intent(this, RecuperarSenhaActivity.class));
    }

}