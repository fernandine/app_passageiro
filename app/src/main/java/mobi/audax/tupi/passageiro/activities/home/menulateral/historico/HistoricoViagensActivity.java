package mobi.audax.tupi.passageiro.activities.home.menulateral.historico;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.adapter.MinhaRotaAdapter;
import mobi.audax.tupi.passageiro.bin.bean.Passageiro;
import mobi.audax.tupi.passageiro.bin.bo.PassageiroBo;
import mobi.audax.tupi.passageiro.bin.dto.Coordenadas;
import mobi.audax.tupi.passageiro.bin.dto.MinhaRota;
import mobi.stos.httplib.HttpAsync;
import mobi.stos.httplib.inter.FutureCallback;

public class HistoricoViagensActivity extends AppCompatActivity {
    private static final String TAG = "HistoricoViagensActivit";
    private Toolbar myToolbar;
    private List<MinhaRota> minhaRotaList;
    private List<Coordenadas> coordenadasParadasList;
    private ListView list;
    private Passageiro passageiro;
    private TextView mTvSemViagens;
    private Button mBtnVoltar;
    private PassageiroBo passageiroBo;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_viagens);
        initViews();
        minhaRotaList = new ArrayList<>();
        coordenadasParadasList = new ArrayList<>();
        getRotas();
    }

    private void initViews() {
        //objs & array init
        passageiro = new PassageiroBo(this).autenticado();
        coordenadasParadasList = new ArrayList<>();
        minhaRotaList = new ArrayList<>();
        passageiroBo = new PassageiroBo(this);
        //toolbar
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        //views
        mBtnVoltar = findViewById(R.id.buttonVoltar);
        mTvSemViagens = findViewById(R.id.textViewListaVazia);
        list = findViewById(R.id.list);
    }

    private void getRotas() {
        try {
            progressDialog = new ProgressDialog(this);
            //todo substituir por minhasRotas
            passageiro = new PassageiroBo(this).autenticado();

            HttpAsync httpAsync = new HttpAsync(new URL(getString(R.string.base_url) + "rota/passageiro/historicoViagens?limite=10&pagina=1&id_passageiro=" + passageiro.getId()));
            httpAsync.addHeader("Authorization", "Bearer " + passageiro.getToken());
            httpAsync.setDebug(true);
            httpAsync.get(new FutureCallback() {
                @Override
                public void onBeforeExecute() {
                    progressDialog.setMessage(getString(R.string.carregando));
                    progressDialog.show();

                }

                @Override
                public void onAfterExecute() {
                    progressDialog.dismiss();
                    list.setAdapter(new MinhaRotaAdapter(HistoricoViagensActivity.this, minhaRotaList));
                    list.setOnItemClickListener((adapterView, view, i, l) -> {
                        MinhaRota minhaRota = (MinhaRota) minhaRotaList.get(i);
                        startActivity(new Intent(HistoricoViagensActivity.this, HistoricoViagensDetalheActivity.class)
                                .putExtra("HISTORICOVIAGEM", minhaRota));
                    });
                }

                @Override
                public void onSuccess(int responseCode, Object object) {

                    switch (responseCode) {
                        case 200:
                            minhaRotaList.clear();
                            try {
                                JSONArray jsonArray = (JSONArray) object;
                                JSONObject jsonObject;
                                JSONObject jsonObjectRotas;
                                JSONObject jsonObjectParadas;

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    MinhaRota minhaRota = new MinhaRota();
                                    List<Coordenadas> coordenadasParadasList = new ArrayList<>();
                                    jsonObject = jsonArray.getJSONObject(i);
                                    jsonObjectRotas = jsonObject.getJSONObject("rota");
                                    minhaRota.setId(jsonObject.getInt("id_paradaembarque"));
                                    minhaRota.setPartida(jsonObjectRotas.getString("inicio"));
                                    minhaRota.setDestino(jsonObjectRotas.getString("fim"));
                                    //dados veiculo
                                    minhaRota.setModeloVeiculo(jsonObject.getString("modelo_veiculo"));
                                    minhaRota.setAnoVeiculo(jsonObject.getInt("ano_veiculo"));
                                    minhaRota.setPlacaVeiculo(jsonObject.getString("placa_veiculo"));
                                    //motorista
                                    minhaRota.setNomeMotorista(jsonObject.getString("nome_motorista"));
                                    minhaRota.setFotoPerfilMotorista(jsonObject.getString("urlFoto"));
                                    if (!jsonObject.isNull("embarcado_at")) {
                                        minhaRota.setEmbarcadoAt(jsonObject.getLong("embarcado_at"));
                                    }else{
                                        minhaRota.setEmbarcadoAt(0);
                                    }
                                    //coordenadas da partida
                                    Coordenadas coordenadasPartida = new Coordenadas();
                                    coordenadasPartida.setLatitude(jsonObjectRotas.getJSONObject("partida").getDouble("latitude"));
                                    coordenadasPartida.setLongitude(jsonObjectRotas.getJSONObject("partida").getDouble("longitude"));
                                    minhaRota.setCoordenadaPartida(coordenadasPartida);


                                    //paradas
                                    JSONArray jsonArrayParadas = jsonObjectRotas.getJSONArray("paradas");
                                    for (int j = 0; j < jsonArrayParadas.length(); j++) {
                                        jsonObjectParadas = jsonArrayParadas.getJSONObject(j);
                                        Coordenadas coordenadasParadas = new Coordenadas();
                                        coordenadasParadas.setLatitude(jsonObjectParadas.getDouble("latitude"));
                                        coordenadasParadas.setLongitude(jsonObjectParadas.getDouble("longitude"));
                                        coordenadasParadasList.add(coordenadasParadas);

                                    }
                                    minhaRota.setListCoordenadas(coordenadasParadasList);

                                    //destino

                                    Coordenadas coordenadasDestino = new Coordenadas();
                                    minhaRota.setId(jsonObject.getInt("id_paradadesembarque"));
                                    coordenadasDestino.setLatitude(jsonObjectRotas.getJSONObject("destino").getDouble("latitude"));
                                    coordenadasDestino.setLongitude(jsonObjectRotas.getJSONObject("destino").getDouble("longitude"));
                                    minhaRota.setCoordenadaDestino(coordenadasDestino);
                                    minhaRotaList.add(minhaRota);

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 204:
                            list.setVisibility(View.GONE);
                            mBtnVoltar.setVisibility(View.VISIBLE);
                            mTvSemViagens.setVisibility(View.VISIBLE);
                            mBtnVoltar.setOnClickListener(view -> finish());
                            break;
                        case 401:
                            refreshToken(1);
                            break;
                        default:
                            progressDialog.dismiss();
                            break;
                    }

                }

                @Override
                public void onFailure(Exception exception) {
                    progressDialog.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshToken(int respondeCodeToken) {
        try {
            passageiro = new PassageiroBo(this).autenticado();

            HttpAsync httpAsync = new HttpAsync(new URL(getString(R.string.base_url) + "passageiro/autenticacao/refreshToken"));
            httpAsync.addParam("hash", passageiro.getHash());
            httpAsync.setDebug(true);
            httpAsync.put(new FutureCallback() {
                @Override
                public void onBeforeExecute() {

                }

                @Override
                public void onAfterExecute() {


                }

                @Override
                public void onSuccess(int responseCode, Object object) {
                    switch (responseCode) {
                        case 201:
                            JSONObject jsonObject = (JSONObject) object;
                            try {
                                passageiro.setToken(jsonObject.getString("token"));
                                passageiro.setHash(jsonObject.getString("hash"));

                                passageiroBo.update(passageiro);
                                if (respondeCodeToken == 1) {
                                    getRotas();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            Toast.makeText(HistoricoViagensActivity.this, "Falha ao autenticar token..", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onFailure(Exception exception) {

                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}