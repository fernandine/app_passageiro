package mobi.audax.tupi.passageiro.activities.cadastro;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.home.HomeActivity;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.activities.splash.SplashActivity;
import mobi.audax.tupi.passageiro.bin.bean.Arquivo;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bean.Pessoa;
import mobi.audax.tupi.passageiro.bin.controller.PassageiroController;
import mobi.audax.tupi.passageiro.bin.dto.GoogleAuth;
import mobi.audax.tupi.passageiro.bin.dto.Login;
import mobi.audax.tupi.passageiro.bin.util.Permission;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;
import mobi.audax.tupi.passageiro.bin.util.WriteSDCard;

public class TirarFotoActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 7;
    private final int REQUEST_TAKE_PHOTO = 1;

    private File photoFile;
    private Passenger passenger;
    private boolean google = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tirar_foto);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        passenger = (Passenger) getIntent().getSerializableExtra(Passenger.class.getSimpleName());
        google = getIntent().getBooleanExtra("google", false);

        findViewById(R.id.btnTirarFoto).setOnClickListener(view -> onTirarFoto());
    }

    private boolean allPermissionsAllowed() {
        var permissionRequest = new ArrayList<String>();
        var permission = new Permission(this);
        if (!permission.hasCameraPermission()) {
            permissionRequest.add(Manifest.permission.CAMERA);
        }
        if (!permission.hasWriteExternalStoragePermission()) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequest.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Nullable
    private File getOutputMediaFile() {
        try {
            return new WriteSDCard(this).createFilePath("IMG_" + Calendar.getInstance().getTimeInMillis() + "_" + REQUEST_TAKE_PHOTO + ".jpg");
        } catch (Exception e) {
            return null;
        }
    }

    public void onTirarFoto() {
        try {
            if (this.allPermissionsAllowed()) {
                var takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    photoFile = getOutputMediaFile();
                    if (photoFile != null) {
                        var uri = FileProvider.getUriForFile(this, "mobi.audax.tupi.passageiro.provider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                } else {
                    adicionarFotoAndSaveUser();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void adicionarFotoAndSaveUser() {
        if (passenger.getPessoa() == null) {
            passenger.setPessoa(new Pessoa());
        }
        if (passenger.getPessoa().getFotoPerfil() == null) {
            passenger.getPessoa().setFotoPerfil(new Arquivo());
        }

        if (photoFile != null && photoFile.exists()) {
            passenger.getPessoa().getFotoPerfil().setCaminho(photoFile.getAbsolutePath());
        } else {
            Toast.makeText(this, R.string.erro_capturar_foto, Toast.LENGTH_LONG).show();
        }

        salvar();
    }

    private void salvar() {
        var caminho = passenger.getPessoa().getFotoPerfil().getCaminho();
        if (caminho != null && !caminho.startsWith("http")) {
            var path = passenger.getPessoa().getFotoPerfil().getCaminho();
            var base64 = Util.imageToBase64(path);
            passenger.getPessoa().getFotoPerfil().setCaminho(base64);
        }

        var controller = new PassageiroController(this);
        controller.create(passenger, () -> {
            dismissProgressDialog();
            progressDialog = ProgressDialog.show(this, null, getString(R.string.realizando_cadastro), true);
            return null;
        }, statusCode -> {
            dismissProgressDialog();

            switch (statusCode) {
                case 201:
                    if (google) {
                        loginGoogle();
                    } else {
                        login();
                    }
                    break;
                case 409:
                    Util.showAlertDialog(getString(R.string.atencao), getString(R.string.passageiro_informado_ja_cadastrado_sistema), this, () -> {
                        finishAffinity();
                        startActivities(new Intent[]{new Intent(this, SplashActivity.class), new Intent(this, LoginActivity.class),});
                    });
                    break;
                case 417:
                    Util.showAlertDialog(getString(R.string.preencha_todos_dados_processeguir_cadastro), this);
                    break;
                default:
                    Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
                    break;
            }

            return null;
        });
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void login() {
        var email = passenger.getEmail();
        var senha = passenger.getSenha();
        var token = new Prefs(this).getToken();

        var controller = new PassageiroController(this);
        controller.login(new Login(email, senha, token), () -> {
            dismissProgressDialog();
            progressDialog = ProgressDialog.show(this, null, getString(R.string.acessando), true);
            return null;
        }, statusCode -> {
            dismissProgressDialog();
            switch (statusCode) {
                case 200 -> {
                    finishAffinity();
                    startActivities(new Intent[]{new Intent(this, SplashActivity.class), new Intent(this, HomeActivity.class)});
                }
                case 401, 412 ->
                        Util.showAlertDialog(getString(R.string.passageiro_nao_encontrado), this);
                case 999 ->
                        Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    private void loginGoogle() {
        var firebase = passenger.getFireBaseId();
        var token = new Prefs(this).getToken();

        var controller = new PassageiroController(this);
        controller.googleLogin(new GoogleAuth(firebase, token), () -> {
            dismissProgressDialog();
            progressDialog = ProgressDialog.show(this, null, getString(R.string.acessando), true);
            return null;
        }, statusCode -> {
            dismissProgressDialog();
            switch (statusCode) {
                case 200 -> {
                    finishAffinity();
                    startActivities(new Intent[]{new Intent(this, SplashActivity.class), new Intent(this, HomeActivity.class)});
                }
                case 401, 412 ->
                        Util.showAlertDialog(getString(R.string.passageiro_nao_encontrado), this);
                case 999 ->
                        Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                adicionarFotoAndSaveUser();
            } else if (resultCode == RESULT_CANCELED) {
                if (photoFile != null && photoFile.exists()) {
                    photoFile.delete();
                }
                Util.alert(this, R.string.atencao, R.string.mensagem_erro_foto_cancelada, R.string.ok);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        var ok = true;
        for (var result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                ok = false;
                break;
            }
        }
        if (ok) {
            onTirarFoto();
        } else {
            Util.showAlertDialog(getString(R.string.voce_precisa_aceitar_permissao_para_tirar_foto), this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}