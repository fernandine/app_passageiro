package mobi.audax.tupi.passageiro.activities.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.bem_vindo.BemVindoActivity;
import mobi.audax.tupi.passageiro.activities.home.HomeActivity;
import mobi.audax.tupi.passageiro.activities.intro.PermissionActivity;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.bo.ViagemBo;
import mobi.audax.tupi.passageiro.bin.util.Permission;
import mobi.audax.tupi.passageiro.bin.util.Prefs;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        var prefs = new Prefs(this);
        String notifica = (String) getIntent().getSerializableExtra("NOTIFICA");
        if (notifica != null) {
            Log.e("NOTIFICA", "onCreate:>>>>>>>>>>>>>>>>> " + notifica);

            prefs.setDesembarcou(new ViagemBo(this).current() != null);
        }
        String embarcou = (String) getIntent().getSerializableExtra("EMBARCOU");
        if (embarcou != null) {
            prefs.setEmbarcou(true);
            Log.e("EMBARCOU", "onCreate:>>>>>>>>>>>>>>>>> " + notifica);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        var handler = new Handler();
        handler.postDelayed(() -> {

            var permission = new Permission(this);
            if (permission.needRequestPermission()) {
                startActivity(new Intent(this, PermissionActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
        }, 2000);
    }

}