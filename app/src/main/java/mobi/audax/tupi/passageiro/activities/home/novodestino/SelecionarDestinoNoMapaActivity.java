package mobi.audax.tupi.passageiro.activities.home.novodestino;

import static com.here.sdk.mapview.LocationIndicator.IndicatorStyle.PEDESTRIAN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.gestures.GestureState;

import com.here.sdk.mapview.LocationIndicator;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapCameraListener;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mobi.audax.tupi.passageiro.bin.bean.Passageiro;
import mobi.audax.tupi.passageiro.bin.controller.RotaPassageiroController;
import mobi.audax.tupi.passageiro.bin.task.location.LocationCommons;
import mobi.audax.tupi.passageiro.bin.task.location.LocationThread;
import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bo.PassageiroBo;
import mobi.audax.tupi.passageiro.bin.dto.Parada;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;
import mobi.stos.httplib.HttpAsync;
import mobi.stos.httplib.inter.FutureCallback;

public class SelecionarDestinoNoMapaActivity extends AppCompatActivity  implements LocationListener {

    private static final String TAG = "SelecionarDestinoNoMapa";
    private MapView mapViewSelecionarDestino;
    private Passageiro passageiro,passageiroLogado;
    private final int REQUEST_CODE_PERMISSIONS = 333;
    private List<MapMarker> mapMarkerSelectedPointsList;
    private List<MapMarker> mapMarkerCurrentLoc;
    private SearchEngine searchEngine;
    private Toolbar myToolbar;
    private TextView mTvToolbar,mTvIndoPara;
    private LinearLayout ll;
    private PassageiroBo passageiroBo;
    private float latitudeOnTap,longitudeOnTap;
    private boolean zoomAcimaDe16 = false;
    private Viagem selecionarLocal;
    private ImageView mIvCurrentLoc;
    private Button mBtnConfirmarRota;
    private List<MapMarker> listParadas;
    private LocationIndicator locationIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_destino_no_mapa);
        initViews();
        mapViewSelecionarDestino = findViewById(R.id.map_view2);
        mapViewSelecionarDestino.onCreate(savedInstanceState);
        setTapGestureHandler(mapViewSelecionarDestino);
        locationIndicator = new LocationIndicator();
        locationIndicator.setLocationIndicatorStyle(PEDESTRIAN);
        addCameraObserver();

      //  mIvCurrentLoc.setOnClickListener(view -> {
       //     mapViewSelecionarDestino.getCamera().flyTo(new GeoCoordinates(passageiro.getLat(), passageiro.getLongitude()));
        //    mapViewSelecionarDestino.getCamera().zoomTo(14);
     //   });
        mBtnConfirmarRota.setOnClickListener(view2 -> {

            startActivity(new Intent(this, DuracaoRotaActivity.class).putExtra("VIAGEM",selecionarLocal));
            finish();
        });

        handleLocationUpdates();

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

    Toast toast;

    private void showToast(int st) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, st, Toast.LENGTH_SHORT);
        toast.show();
    }

    private final MapCameraListener cameraListener = new MapCameraListener() {
        @Override
        public void onMapCameraUpdated(@NonNull MapCamera.State state) {
            GeoCoordinates camTarget = state.targetCoordinates;
            Log.d("CameraListener", "New camera target: " +
                    camTarget.latitude + ", " + camTarget.longitude);

            mapViewSelecionarDestino.getGestures().setPanListener((gestureState, point2D, point2D1, v) -> {
                if (gestureState == GestureState.END && state.zoomLevel > 16) {
                     carregarParadas();

                } else if (gestureState == GestureState.END && state.zoomLevel < 16 && zoomAcimaDe16 == false) {
                    zoomAcimaDe16 = true;
                    Toast.makeText(SelecionarDestinoNoMapaActivity.this, R.string.aproxime_para_ver_paradas, Toast.LENGTH_SHORT).show();
                }

            });
            mapViewSelecionarDestino.getGestures().setPinchRotateListener((gestureState, point2D, point2D1, v, angle) -> {
                if (gestureState == GestureState.END && state.zoomLevel > 16) {
                    carregarParadas();
                } else if (gestureState == GestureState.END && state.zoomLevel < 16 && zoomAcimaDe16 == false) {
                    zoomAcimaDe16 = true;
                    Toast.makeText(SelecionarDestinoNoMapaActivity.this, R.string.aproxime_para_ver_paradas, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void addCameraObserver() {
        mapViewSelecionarDestino.getCamera().addListener(cameraListener);
    }

    private void initViews() {
        //objs & arrays
        selecionarLocal = new Viagem();
        passageiroBo = new PassageiroBo(this);
        passageiroLogado = new PassageiroBo(this).autenticado();
        //toolbar
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        mTvToolbar = findViewById(R.id.textViewLocalToolbar);
        mTvToolbar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTvToolbar.setSelected(true);
        mTvIndoPara = findViewById(R.id.textView);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.vc_back_white_arrow);
        myToolbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#16163F")));
        listParadas = new ArrayList<>();

        //views
        mIvCurrentLoc = findViewById(R.id.ivCurrentLoc);
        ll = findViewById(R.id.ll);
        mBtnConfirmarRota = findViewById(R.id.button_Confirmar_Rota);
        // obj & array init
        mapMarkerSelectedPointsList = new ArrayList<>();
        mapMarkerCurrentLoc = new ArrayList<>();
        passageiro = new PassageiroBo(this).autenticado();
        //shared

    }

    private void clearWaypointMapMarker() {
        for (var mapMarker : mapMarkerSelectedPointsList) {
            mapViewSelecionarDestino.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerSelectedPointsList.clear();
    }

    private void clearParadasMapMarker() {
        for (var mapMarker : listParadas) {
            mapViewSelecionarDestino.getMapScene().removeMapMarker(mapMarker);
        }
        listParadas.clear();
    }

    private void clearCurrentLoc() {
        for (var mapMarker : mapMarkerCurrentLoc) {
            mapViewSelecionarDestino.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerCurrentLoc.clear();
    }

    private void loadMapScene() {
        mapViewSelecionarDestino.getMapScene().loadScene(MapScheme.NORMAL_DAY, mapError -> {
                if (mapError == null) {
                    var prefs = new Prefs(SelecionarDestinoNoMapaActivity.this);
                    mapViewSelecionarDestino.getCamera().flyTo(new GeoCoordinates(prefs.getLatitude(), prefs.getLongitude()));
                    mapViewSelecionarDestino.addLifecycleListener(locationIndicator);

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



        private void setTapGestureHandler(@NonNull MapView mapView) {
        mapView.getGestures().setTapListener(touchPoint -> {
            var geoCoordinates = mapView.viewToGeoCoordinates(touchPoint);
            Log.d(TAG, "Tap at: " + geoCoordinates.latitude +geoCoordinates.longitude);
            var prefs = new Prefs(SelecionarDestinoNoMapaActivity.this);
            clearWaypointMapMarker();
            latitudeOnTap = (float) geoCoordinates.latitude;
            longitudeOnTap = (float) geoCoordinates.longitude;
            Log.e(TAG, "setTapGestureHandler: " + latitudeOnTap );
            Log.e(TAG, "setTapGestureHandler: " + longitudeOnTap );
            selecionarLocal.setLatitudeDesembarque(latitudeOnTap);
            selecionarLocal.setLongitudeDesembarque(longitudeOnTap);
            selecionarLocal.setLatitudeEmbarque(prefs.getLatitude());
            selecionarLocal.setLongitudeEmbarque(prefs.getLongitude());
            addPassageiroMarker(geoCoordinates,R.drawable.ic_user_location_pick,mapMarkerSelectedPointsList);
            getAddressForCoordinates(geoCoordinates);
        });}

    private void getAddressForCoordinates(GeoCoordinates geoCoordinates) {
        int maxItems = 1;
        SearchOptions reverseGeocodingOptions = new SearchOptions(LanguageCode.EN_GB, maxItems);
        try {
            searchEngine= new SearchEngine();
        } catch (InstantiationErrorException e) {
            e.printStackTrace();
        }
        searchEngine.search(geoCoordinates, reverseGeocodingOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                if (searchError != null) {
                    Toast.makeText(SelecionarDestinoNoMapaActivity.this, ""+searchError.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
                // If error is null, list is guaranteed to be not empty.
                mTvIndoPara.setText(R.string.indo_para); //add numero auqi caso nao apareça;
                mTvToolbar.setText(""+list.get(0).getAddress().addressText); //add numero auqi caso nao apareça;
                selecionarLocal.setCidade(list.get(0).getAddress().city);
                if (list.get(0).getAddress().addressText != null) {
                    selecionarLocal.setLogradouro(list.get(0).getAddress().street);
                }
                else{
                    selecionarLocal.setLogradouro(list.get(0).getAddress().city);
                }
                ll.setVisibility(View.VISIBLE);


            }
        });
    }

    private void handleLocationUpdates() {
        var commons = new LocationCommons();
        if (commons.isLocationEnabled(this)) {
            var thread = new LocationThread(this, location -> {
                clearCurrentLoc();

    //            addPassageiroMarker(new GeoCoordinates(location.getLatitude(),location.getLongitude()),R.drawable.ic_passageiro,mapMarkerCurrentLoc);
                return null;
            });
            thread.requestLocation();
            loadMapScene();
        }
        else {
            Toast.makeText(this, R.string.por_favor_ative_sua_localizacao, Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public void addPassageiroMarker(GeoCoordinates geoCoordinates, int resourceId, @NonNull List list) {
        var mapImage = MapImageFactory.fromResource(this.getResources(), resourceId);
        var anchor = new Anchor2D(1.0f,0.5f);
        var mapMarker = new MapMarker(geoCoordinates,mapImage,anchor);
        mapViewSelecionarDestino.getMapScene().addMapMarker(mapMarker);
        list.add(mapMarker);
    }

    public void addParadasMarker(@NonNull List<MapMarker> list) {
        for (var o : list) {
            mapViewSelecionarDestino.getMapScene().addMapMarker(o);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapViewSelecionarDestino.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapViewSelecionarDestino.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewSelecionarDestino.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCurrentLocationD(View view) {
        final var vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));


        var controller = new LocationThread(this, location -> {
            this.updateLocationIndicator(location);
            //  mapView.getCamera().flyTo(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
            mapViewSelecionarDestino.getCamera().lookAt(new GeoCoordinates(location.getLatitude(), location.getLongitude()),1000);

            return null;
        });
        controller.setUseOnlyLastKnowLocation(true);
        controller.requestLocation();
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
        mapViewSelecionarDestino.getCamera().flyTo(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
        Log.e(TAG, "onLocationChanged chama chama : "  );

        var prefs = new Prefs(this);
        prefs.recordLocation(location);

        this.updateLocationIndicator(location);
    }
}