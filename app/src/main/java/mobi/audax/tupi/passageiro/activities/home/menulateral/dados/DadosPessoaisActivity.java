package mobi.audax.tupi.passageiro.activities.home.menulateral.dados;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;

public class DadosPessoaisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_pessoais);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataBiding();
    }

    private void dataBiding() {
        var passenger = new PassengerBo(this).autenticado();
        if (passenger != null && passenger.getPessoa() != null) {
            ((TextView) findViewById(R.id.nome)).setText(passenger.getPessoa().getNome());
            ((TextView) findViewById(R.id.email)).setText(passenger.getEmail());
            ((TextView) findViewById(R.id.telefone)).setText(passenger.getPessoa().getCelular());
            if (passenger.getPessoa().getFotoPerfil() != null) {
                Picasso.get().load(passenger.getPessoa().getFotoPerfil().getCaminho()).placeholder(R.drawable.vc_logo_tupi).into((CircleImageView) findViewById(R.id.foto));
            }
        }
    }

    public void onEditar(View view) {
        startActivity(new Intent(this,EditarDadosActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}