package mobi.audax.tupi.passageiro.activities.home.novodestino;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.adapter.DuracaoRotasAdapter;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;
import mobi.audax.tupi.passageiro.bin.controller.RotaPassageiroController;
import mobi.audax.tupi.passageiro.bin.dto.Coordenadas;
import mobi.audax.tupi.passageiro.bin.dto.ParadaDestino;
import mobi.audax.tupi.passageiro.bin.dto.ParadaEmbarque;
import mobi.audax.tupi.passageiro.bin.dto.ViagemMotorista;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class DuracaoRotaActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ListView listView;
    private DuracaoRotasAdapter adapter;
    private Viagem viagem;
    private Button mBtnVoltar;
    private TextView mTvSemViagens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duracao_rota);
        initViews();
        calcularRotas();
    }

    private void calcularRotas() {
        Log.e("DE", "calcularRotas: " + viagem.getLatitudeDesembarque() + " " + viagem.getLongitudeDesembarque());
        var destiny = new ParadaDestino();
        destiny.setDestino(new Coordenadas(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque()));
        destiny.setLatitude(viagem.getLatitudeEmbarque());
        destiny.setLongitude(viagem.getLongitudeEmbarque());

        var controller = new RotaPassageiroController(this);
        controller.paradasDestino(destiny, () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.calculando_rotas), true);
            return null;
        }, (statusCode, list) -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (statusCode) {
                case 200 -> {
                    adapter.addAll(list);
                    adapter.notifyDataSetChanged();
                }
                case 204 -> {
                    listView.setVisibility(View.GONE);
                    mBtnVoltar.setVisibility(View.VISIBLE);
                    mTvSemViagens.setVisibility(View.VISIBLE);
                }
                default ->
                        Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    private void rotaDaViagem(@NonNull ParadaEmbarque paradaEmbarque) {
        Log.e("DE", "rotaDaViagem:  ");
        var controller = new RotaPassageiroController(this);
        controller.rotaViagem(paradaEmbarque.getViagem(), () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.gerando_rota), true);
            return null;
        }, (statusCode, rota) -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (statusCode == 200) {
                var prefs = new Prefs(DuracaoRotaActivity.this);
                prefs.setDesembarcou(false);  //reseta o desembarque
                prefs.setEmbarcou(false);    //reseta a embarcação

                prefs.setViagemId(paradaEmbarque.getViagem());

                viagem.setViagem(paradaEmbarque.getViagem());
                viagem.setParadaEmbarque(paradaEmbarque.getEmbarque().getId());
                viagem.setLatitudeEmbarque(paradaEmbarque.getEmbarque().getLatitude());
                viagem.setLongitudeEmbarque(paradaEmbarque.getEmbarque().getLongitude());

                viagem.setParadaDesembarque(paradaEmbarque.getDestino().getId());
                viagem.setLatitudeDesembarque(paradaEmbarque.getDestino().getLatitude());
                viagem.setLongitudeDesembarque(paradaEmbarque.getDestino().getLongitude());

                viagem.setMotorista(paradaEmbarque.getMotoristaViagem().getMotorista());
                viagem.setPlaca(paradaEmbarque.getMotoristaViagem().getPlaca());
                viagem.setModelo(paradaEmbarque.getMotoristaViagem().getModelo());
                viagem.setMarca(paradaEmbarque.getMotoristaViagem().getMarca());
                viagem.setFotoMotorista(paradaEmbarque.getMotoristaViagem().getFoto());

                viagem.setValorPassagem(paradaEmbarque.getValorPassagem());

                var viagemMotorista = new ViagemMotorista();
                viagemMotorista.setCoordenadaPartida(new Coordenadas(rota.getPartida().getLatitude(), rota.getPartida().getLongitude()));
                viagemMotorista.setCoordenadaDestino(new Coordenadas(rota.getDestino().getLatitude(), rota.getDestino().getLongitude()));

                var paradas = new ArrayList<Coordenadas>();
                if (rota.getParadas() != null) {
                    rota.getParadas().forEach(p -> paradas.add(new Coordenadas(p.getLatitude(), p.getLongitude())));
                }
                viagemMotorista.setListCoordenadas(paradas);
                viagemMotorista.setCoordenadasDestinoPassageiro(new Coordenadas(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque()));

                startActivity(
                        new Intent(this, NavigationActivity.class)
                                .putExtra("VIAGEMMOTORISTA", viagemMotorista)
                                .putExtra("VIAGEM", viagem)
                );
                finish();
            } else {
                Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });

    }

    public void onVoltar(View view) {
        onBackPressed();
    }

    private void initViews() {
        viagem = (Viagem) getIntent().getSerializableExtra("VIAGEM");

        var toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.indo_para));
        toolbar.setSubtitle(viagem.getNome());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTvSemViagens = findViewById(R.id.textViewListaVazia);
        mBtnVoltar = findViewById(R.id.buttonVoltar);

        this.adapter = new DuracaoRotasAdapter(this);
        this.listView = (ListView) findViewById(R.id.list);
        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener((adapterView, view, item, l) -> {
            var paradaEmbarque = adapter.getItem(item);
            rotaDaViagem(paradaEmbarque);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}