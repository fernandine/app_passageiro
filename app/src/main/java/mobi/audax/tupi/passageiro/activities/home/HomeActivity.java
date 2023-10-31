package mobi.audax.tupi.passageiro.activities.home;

import static com.here.sdk.mapview.LocationIndicator.IndicatorStyle.PEDESTRIAN;
import static mobi.audax.tupi.passageiro.bin.util.Util.showAlertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.gestures.GestureState;
import com.here.sdk.mapview.LocationIndicator;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapCameraListener;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.home.menulateral.dados.DadosPessoaisActivity;
import mobi.audax.tupi.passageiro.activities.home.menulateral.historico.HistoricoViagensActivity;
import mobi.audax.tupi.passageiro.activities.home.menulateral.pagamento.PagamentoActivity;
import mobi.audax.tupi.passageiro.activities.home.novodestino.DefinirLocalActivity;
import mobi.audax.tupi.passageiro.activities.home.novodestino.NavigationActivity;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.adapter.ViagensFrequentesAdapter;
import mobi.audax.tupi.passageiro.bin.bean.MinhaRota;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;
import mobi.audax.tupi.passageiro.bin.bo.MinhaRotaBo;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.bo.ViagemBo;
import mobi.audax.tupi.passageiro.bin.bo.ViagemFrequenteBo;
import mobi.audax.tupi.passageiro.bin.controller.HistoricoController;
import mobi.audax.tupi.passageiro.bin.controller.RotaPassageiroController;
import mobi.audax.tupi.passageiro.bin.dto.Coordenadas;
import mobi.audax.tupi.passageiro.bin.dto.ViagemMotorista;
import mobi.audax.tupi.passageiro.bin.task.location.LocationCommons;
import mobi.audax.tupi.passageiro.bin.task.location.LocationThread;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    int LAUNCH_SECOND_ACTIVITY = 1;

    private static final String TAG = "HomeActivity";
    private DrawerLayout drawerLayout;
    private ListView listView;
    private ProgressDialog progressDialog;

    private List<MinhaRota> minhaRotaList;

    private ImageView ivSlideUp;
    private boolean zoomAcimaDe16 = false;
    private Toast toast;
    private List<MapMarker> listParadas;
    private final List<MapMarker> mapMarkerPassageiroList = new ArrayList<>();
    private MapView mapView;
    private TextView mTvSemRotas;
    private LocationIndicator locationIndicator;
    private Passenger passageiro;
    private AppCompatButton botaoSair;
    private LinearLayout linearLayoutViagensFrequentes;

    // TO DO Refatorar initView e onActivityResult para reaproveitar codigo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        locationIndicator = new LocationIndicator();
        locationIndicator.setLocationIndicatorStyle(PEDESTRIAN);

        var prefs = new Prefs(this);
        if (prefs.getDesembarcou()) {
            new ViagemBo(this).clean();
        }

        passageiro = new PassengerBo(this).autenticado();

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        linearLayoutViagensFrequentes = findViewById(R.id.layoutViagens);
        botaoSair = findViewById(R.id.sair);

        if (passageiro != null) {
            botaoSair.setText(R.string.sair);
            initViewGeneric();
            carregarViagensFrequentes();
            carregarHistoricoViagens();
        } else {
            botaoSair.setText(R.string.entrar);
            initViewsNaoAuthenticado();
        }

    }

    private void addCameraObserver() {
        mapView.getCamera().addListener(cameraListener);
    }

    private void loadMapScene() {
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, mapError -> {
            if (mapError == null) {
                var prefs = new Prefs(this);
                mapView.getCamera().flyTo(new GeoCoordinates(prefs.getLatitude(), prefs.getLongitude()));
                mapView.addLifecycleListener(locationIndicator);

                var location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(prefs.getLatitude());
                location.setLongitude(prefs.getLongitude());
                location.setAccuracy(prefs.getPrecisao());
                location.setBearing(prefs.getBearing());
                this.updateLocationIndicator(location);
            } else {
                Log.e(TAG, "onLoadScene failed: " + mapError);
            }
        });
    }

    private void verificaViagem(Viagem viagem) {

        var controller = new RotaPassageiroController(this);
        controller.rotaViagem(viagem.getViagem(), () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.gerando_rota), true);
            return null;
        }, (statusCode, rota) -> {

            Log.e(TAG, "verificaViagem rota id: " + rota.getId());

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (statusCode == 200) {
                var entity = new ViagemMotorista();
                entity.setCoordenadaPartida(new Coordenadas(rota.getPartida().getLatitude(), rota.getPartida().getLongitude()));
                entity.setCoordenadaDestino(new Coordenadas(rota.getDestino().getLatitude(), rota.getDestino().getLongitude()));

                List<Coordenadas> paradas = new ArrayList<>();
                rota.getParadas().forEach(p -> {
                    paradas.add(new Coordenadas(p.getLatitude(), p.getLongitude()));
                });
                entity.setListCoordenadas(paradas);
                entity.setCoordenadasDestinoPassageiro(new Coordenadas(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque()));
                startActivity(
                        new Intent(this, NavigationActivity.class)
                                .putExtra("VIAGEMMOTORISTA", entity)
                                .putExtra("VIAGEM", viagem)
                );

            } else {
                Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });

    }

    private void viagemAtiva() {
        //  var viagem = new ViagemBo(this).current();
        //    if (viagem != null){
        //        verificaViagem(viagem);
        //    }else{
        var viagemBo = new ViagemBo(this);
        viagemBo.clean();

        var passenger = new PassengerBo(this).autenticado();
        if (passenger != null) {
            var controller = new RotaPassageiroController(this);
            controller.rotaPassageiroAtivo(Objects.requireNonNull(passenger).getId(), () -> {
                progressDialog = ProgressDialog.show(this, null, "Carregando ...", true);
                return null;
            }, (statusCode, rota) -> {

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (statusCode == 200) {

                    if (!rota.getDesembarcou() && !rota.getPassagemCancelada()) {
                        var prefs = new Prefs(HomeActivity.this);
                        prefs.setDesembarcou(false);  //reseta o desembarque
                        if (rota.getEmbarcou()) {
                            prefs.setEmbarcou(true); //embarco o passageiro
                        }
                        var viagemAtiva = new Viagem();
                        viagemAtiva.setViagem(rota.getId());

                        viagemAtiva.setParadaEmbarque(rota.getEmbarque().getId());
                        viagemAtiva.setLatitudeEmbarque(rota.getEmbarque().getLatitude());
                        viagemAtiva.setLongitudeEmbarque(rota.getEmbarque().getLongitude());

                        viagemAtiva.setParadaDesembarque(rota.getDesembarque().getId());
                        viagemAtiva.setNomeParadaDesembarque(rota.getDesembarque().getNome());
                        viagemAtiva.setLatitudeDesembarque(rota.getDesembarque().getLatitude());
                        viagemAtiva.setLongitudeDesembarque(rota.getDesembarque().getLongitude());

                        viagemAtiva.setMotorista(rota.getMotoristaViagem().getMotorista());
                        viagemAtiva.setPlaca(rota.getMotoristaViagem().getPlaca());
                        viagemAtiva.setModelo(rota.getMotoristaViagem().getModelo());
                        viagemAtiva.setMarca(rota.getMotoristaViagem().getMarca());
                        viagemAtiva.setFotoMotorista(rota.getMotoristaViagem().getFoto());

                        viagemBo.insert(viagemAtiva);

                        verificaViagem(viagemAtiva);
                    } else {
                        addCameraObserver();
                        Log.e(TAG, "viagemAtiva Boolean : ELSE >>>>>>>>>>>>>>");
                    }

                } else {
                    addCameraObserver();
                    Log.e(TAG, "viagemAtiva: ELSE >>>>>>>>>>>>>>");
                }
                return null;
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.handleLocationUpdates();
        mapView.onResume();
        dadosPassageiro();
        viagemAtiva();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_historicodeviagens ->
                    startActivity(new Intent(this, HistoricoViagensActivity.class));
            case R.id.nav_pagamentos -> startActivity(new Intent(this, PagamentoActivity.class));
            case R.id.nav_dadosPessoais ->
                    startActivity(new Intent(this, DadosPessoaisActivity.class));
            default -> {
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void dadosPassageiro() {
        // region define dados de exibição do passageiro
        var navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        navView.setItemIconTintList(null);

        var passenger = new PassengerBo(this).autenticado();
        if (passenger != null) {
            var headerView = navView.getHeaderView(0);
            var nome = (TextView) headerView.findViewById(R.id.tvNavHeaderNome);
            var foto = (CircleImageView) headerView.findViewById(R.id.imageView);

            if (passenger.getPessoa() != null) {
                nome.setText(passenger.getPessoa().getNome());
                if (passenger.getPessoa().getFotoPerfil() != null && StringUtils.isNotBlank(passenger.getPessoa().getFotoPerfil().getCaminho())) {
                    var path = passenger.getPessoa().getFotoPerfil().getCaminho();
                    Log.v("FOTO", "url: " + path);
                    Picasso.get().load(path).into(foto);
                }
            }
        } else {
            var headerView = navView.getHeaderView(0);
            var nome = (TextView) headerView.findViewById(R.id.tvNavHeaderNome);
            var foto = (CircleImageView) headerView.findViewById(R.id.imageView);
            nome.setText(R.string.app_name);
            foto.setImageResource(R.mipmap.ic_launcher_icon);
        }
    }

    public void onCurrentLocation(View view) {
        final var vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

        var controller = new LocationThread(this, location -> {
            this.updateLocationIndicator(location);
            //  mapView.getCamera().flyTo(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
            mapView.getCamera().lookAt(new GeoCoordinates(location.getLatitude(), location.getLongitude()), 1000);

            return null;
        });
        controller.setUseOnlyLastKnowLocation(true);
        controller.requestLocation();
    }

    private void clearPassageiroMapMarker() {
        for (var mapMarker : mapMarkerPassageiroList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerPassageiroList.clear();
    }

    @SuppressLint("MissingPermission")
    private void handleLocationUpdates() {
        var locationCommons = new LocationCommons();
        if (locationCommons.isLocationEnabled(this)) {
            clearPassageiroMapMarker();

            var locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            loadMapScene();
        } else {
            showAlertDialog(
                    getString(R.string.atencao),
                    getString(R.string.a_partir_desse_ponto_sua_localizacao_deve_esta_habilitada),
                    this,
                    () -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            );
        }
    }

    private void updateLocationIndicator(@NonNull Location location) {
        var data = new com.here.sdk.core.Location.Builder()
                .setCoordinates(new GeoCoordinates(location.getLatitude(), location.getLongitude()))
                .setTimestamp(new Date())
                .setBearingInDegrees((double) location.getBearing())
                .build();

        locationIndicator.updateLocation(data);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mapView.getCamera().flyTo(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
        Log.e(TAG, "onLocationChanged chama chama : ");

        var prefs = new Prefs(this);
        prefs.recordLocation(location);

        this.updateLocationIndicator(location);
    }

    private final MapCameraListener cameraListener = new MapCameraListener() {
        @Override
        public void onMapCameraUpdated(@NonNull MapCamera.State state) {
            mapView.getGestures().setPanListener((gestureState, point2D, point2D1, v) -> {
                if (gestureState == GestureState.END && state.zoomLevel > 16) {
                    carregarParadas();

                } else if (gestureState == GestureState.END && state.zoomLevel < 16 && !zoomAcimaDe16) {
                    zoomAcimaDe16 = true;
                    showToast(R.string.aproxime_para_ver_paradas);
                }

            });
            mapView.getGestures().setPinchRotateListener((gestureState, point2D, point2D1, v, angle) -> {
                if (gestureState == GestureState.END && state.zoomLevel > 16) {
                    carregarParadas();
                } else if (gestureState == GestureState.END && state.zoomLevel < 16 && !zoomAcimaDe16) {
                    zoomAcimaDe16 = true;
                    showToast(R.string.aproxime_para_ver_paradas);
                }
            });
        }
    };

    private void showToast(int st) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, st, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void carregarParadas() {
        var controller = new RotaPassageiroController(this);
        controller.paradasProximas((statusCode, list) -> {

            clearParadasMapMarker();
            switch (statusCode) {
                case 200 -> {
                    for (var pp : list) {
                        var geoCoordinates = new GeoCoordinates(pp.getLatitude(), pp.getLongitude());
                        var mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_paradas);
                        var mapMarker = new MapMarker(geoCoordinates, mapImage);

                        listParadas.add(mapMarker);
                    }
                    addParadasMarker(listParadas);
                }
                case 204 -> showToast(R.string.nenhuma_parada_encontrada_proximo_voce);
                default -> showToast(R.string.erro_estabelecer_conexao_servidor);
            }

            return null;
        });
    }

    private void carregarViagensFrequentes() {
        var controller = new RotaPassageiroController(this);
        controller.viagensFrequentes(statusCode -> {
            switch (statusCode) {
                case 200 -> {
                    var list = new ViagemFrequenteBo(this).list("id DESC");
                    Log.e(TAG, "carregarViagensFrequentes BO : " + new ViagemFrequenteBo(this).list().size());
                    //    listView.setAdapter(new ViagensFrequentesAdapter(HomeActivity.this, list));
                }
                case 204 -> {
                    listView.setVisibility(View.GONE);
                    mTvSemRotas.setVisibility(View.VISIBLE);
                }
                case 500 ->
                        showAlertDialog(getString(R.string.atencao), getString(R.string.erro_estabelecer_conexao_servidor), this);
            }

            carregarParadas();
            return null;
        });
    }

    public void addParadasMarker(@NonNull List<MapMarker> list) {
        list.forEach(marker -> mapView.getMapScene().addMapMarker(marker));
    }

    private void clearParadasMapMarker() {
        for (var mapMarker : listParadas) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        listParadas.clear();
    }

    private void initViewsNaoAuthenticado() {
        linearLayoutViagensFrequentes.setVisibility(View.GONE);
        initViewGeneric();
    }

    private void initViewGeneric() {
        listParadas = new ArrayList<>();
        minhaRotaList = new ArrayList<>();

        listView = findViewById(R.id.listView);
        ivSlideUp = findViewById(R.id.ivSlideUp);

        var toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mTvSemRotas = findViewById(R.id.textViewListaVazia);
        drawerLayout = findViewById(R.id.drawerLayout);

        var toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
        toggle.setHomeAsUpIndicator(R.drawable.vc_buttom_nav_menu);
        toggle.setToolbarNavigationClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
            var passenger = new PassengerBo(this).autenticado();
            if (passenger == null) {
                showMessageNaoAutenticado();
            }
        });

        var slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingUp);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                ivSlideUp.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
    }

    public void onDefineLocal(View view) {
        startActivity(new Intent(this, DefinirLocalActivity.class));
    }

    public void onSair(View view) {
        if (passageiro != null) {
            Util.alert(this, R.string.sair_interrogacao, R.string.deseja_deslogar_seu_usuario_tupi, R.string.sair, () -> {
                var passagerBo = new PassengerBo(this);
                passagerBo.logout();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
            }, R.string.continuar_conectado);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
        }
    }

    private void showMessageNaoAutenticado() {
        Util.alert(this, R.string.usuario_nao_autenticado, R.string.necessario_autenticacao, R.string.entrar_ou_criar_conta, () -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
        }, R.string.ok);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        passageiro = new PassengerBo(this).autenticado();
        linearLayoutViagensFrequentes = findViewById(R.id.layoutViagens);
        if (resultCode == Activity.RESULT_OK) {
            boolean isLoggedIn = data.getBooleanExtra("isLoggedIn", false);
            if (passageiro != null) {
                botaoSair.setText(R.string.sair);
                carregarViagensFrequentes();
                carregarHistoricoViagens();
                dadosPassageiro();
            } else {
                botaoSair.setText(R.string.entrar);
                dadosPassageiro();
            }
        } else {
            botaoSair.setText(R.string.entrar);
            dadosPassageiro();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        passageiro = new PassengerBo(this).autenticado();
//        linearLayoutViagensFrequentes = findViewById(R.id.layoutViagens);
    }

    private void carregarHistoricoViagens() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        HistoricoController controller = new HistoricoController(this);
        controller.getListHistorico(passageiro, () -> {
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }, (statusCode, object) -> {
            progressDialog.dismiss();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            switch (statusCode) {
                case 200:
                    minhaRotaList.clear();
                    try {
                        JSONArray jsonArray = (JSONArray) object;
                        JSONObject jsonObject;
                        JSONObject jsonObjectRotas;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            if (!jsonObject.isNull("rota")) {
                                MinhaRota minhaRota = new MinhaRota();
                                jsonObjectRotas = jsonObject.getJSONObject("rota");
                                minhaRota.setPartida(jsonObjectRotas.getString("inicio"));
                                minhaRota.setDestino(jsonObjectRotas.getString("fim"));
                                minhaRotaList.add(minhaRota);
                            }
                        }

                        MinhaRotaBo minhaRotaBo = new MinhaRotaBo(this);
                        minhaRotaBo.clean();
                        minhaRotaBo.insert(minhaRotaList);

                        listView.setAdapter(new ViagensFrequentesAdapter(HomeActivity.this, minhaRotaBo.list(null, null, "id DESC", 3)));
                        listView.setOnItemClickListener((adapterView, view, i1, l) -> {
                            //    MinhaRota minhaRot = (MinhaRota) minhaRotaList.get(i1);
                            //    startActivity(new Intent(HomeActivity.this, HistoricoViagensDetalheActivity.class)
                            //     .putExtra("HISTORICOVIAGEM", minhaRot));
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 204:

                    break;
                case 401:
                    break;
                default:
                    progressDialog.dismiss();
                    break;
            }
        });
    }

}