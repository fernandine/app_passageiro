package mobi.audax.tupi.passageiro.activities.home.novodestino;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchOptions;

import org.jetbrains.annotations.Contract;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.adapter.SelecionarDestinoAdapter;
import mobi.audax.tupi.passageiro.bin.controller.MapsApiController;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;
import mobi.audax.tupi.passageiro.bin.dto.Coordenadas;
import mobi.audax.tupi.passageiro.bin.util.Prefs;

public class DefinirLocalActivity extends AppCompatActivity {

    private AutoCompleteTextView mEtSelecionarDestino, mEtEditarPartida;

    private SelecionarDestinoAdapter adapter;
    private SearchEngine searchEngine;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Coordenadas from = new Coordenadas();

    private int code = -1;

    private static final int PARTIDA = 2;
    private static final int DESTINO = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definir_local);
        initViews();

        var pref = new Prefs(this);
        getAddressForCoordinates(new GeoCoordinates(pref.getLatitude(), pref.getLongitude()));
        mEtSelecionarDestino.addTextChangedListener(onSearchWatcher(DESTINO));

        from.setLatitude(pref.getLatitude());
        from.setLongitude(pref.getLongitude());
    }

    public void onSelecionarDestinoNoMapa(View view) {
        startActivity(new Intent(this, SelecionarDestinoNoMapaActivity.class));
        finish();
    }

    @NonNull
    @Contract("_ -> new")
    private TextWatcher onSearchWatcher(final int kode) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0 || s.length() > 3) {
                    getListLocais(s, kode);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private void getAddressForCoordinates(GeoCoordinates geoCoordinates) {
        var so = new SearchOptions();
        so.languageCode = LanguageCode.PT_BR;
        so.maxItems = 1;

        try {
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            e.printStackTrace();
        }
        searchEngine.search(geoCoordinates, so, (searchError, list) -> {
            if (searchError != null) {
                Toast.makeText(this, String.valueOf(searchError), Toast.LENGTH_SHORT).show();
                return;
            }

            mEtEditarPartida.setText(list.get(0).getAddress().street + ", " + list.get(0).getAddress().city);
            mEtEditarPartida.addTextChangedListener(onSearchWatcher(PARTIDA));
        });
    }

    private void initViews() {
        this.mEtSelecionarDestino = findViewById(R.id.editTextSelecionarDestino);
        this.mEtEditarPartida = findViewById(R.id.editTextSelecionarPartida);

        this.adapter = new SelecionarDestinoAdapter(this);
        var listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(this.adapter);
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            var viagem = this.adapter.getItem(position);
            if (this.code == 1) {
                viagem.setLatitudeEmbarque(from.getLatitude());
                viagem.setLongitudeEmbarque(from.getLongitude());
                startActivity(new Intent(this, DuracaoRotaActivity.class)
                        .putExtra("VIAGEM", viagem)
                );
                finish();
            } else {
                this.from.setLatitude(viagem.getLatitudeDesembarque());
                this.from.setLongitude(viagem.getLongitudeDesembarque());
                this.mEtEditarPartida.setText(viagem.getNome());
                this.mEtEditarPartida = (AutoCompleteTextView) this.getCurrentFocus();
                if (view != null) {
                    var imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });


        var toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        this.swipeRefreshLayout.setOnRefreshListener(() -> {
            getListLocais(code == PARTIDA ? mEtEditarPartida.getEditableText().toString() : mEtSelecionarDestino.getEditableText().toString(), this.code);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getListLocais(@NonNull CharSequence query, int code) {
        this.code = code;

        var controller = new MapsApiController(this);
        controller.autoSuggest(query.toString(), () -> {
            swipeRefreshLayout.setRefreshing(true);
            return null;
        }, (statusCode, autoSuggest) -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.clear();

            if (statusCode == 200) {
                autoSuggest.getResults().forEach(entity -> {
                    var local = new Viagem();
                    local.setNome(entity.getTitle());
                    var partes = entity.getVicinity().split("<br/>");
                    local.setLogradouro(partes[0]);
                    if (partes.length > 1) {
                        local.setBairro(partes[1]);
                    }
                    if (partes.length > 2) {
                        local.setCidade(partes[2]);
                    }
                    if (partes.length > 3) {
                        local.setCep(partes[3]);
                    }
                    local.setLatitudeDesembarque(entity.getPosition()[0]);
                    local.setLongitudeDesembarque(entity.getPosition()[1]);

                    adapter.add(local);
                });
            } else {
                Toast.makeText(this, R.string.erro_encontrar_locais, Toast.LENGTH_SHORT).show();
            }
            adapter.notifyDataSetChanged();
            return null;
        });
    }

}