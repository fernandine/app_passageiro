package mobi.audax.tupi.passageiro.activities.home.novodestino;

import static com.here.sdk.mapview.LocationIndicator.IndicatorStyle.PEDESTRIAN;
import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LocationIndicator;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchOptions;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.home.menulateral.pagamento.AdicionarNovoCartaoActivity;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.bin.bean.Cartao;
import mobi.audax.tupi.passageiro.bin.bean.Passageiro;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;
import mobi.audax.tupi.passageiro.bin.bo.CartaoBo;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.bo.ViagemBo;
import mobi.audax.tupi.passageiro.bin.controller.RotaPassageiroController;
import mobi.audax.tupi.passageiro.bin.dto.ConfirmarViagem;
import mobi.audax.tupi.passageiro.bin.dto.FinalizarViagem;
import mobi.audax.tupi.passageiro.bin.dto.Pagamento;
import mobi.audax.tupi.passageiro.bin.dto.PassageiroNaoCadastrado;
import mobi.audax.tupi.passageiro.bin.dto.PosicaoViagemRequest;
import mobi.audax.tupi.passageiro.bin.dto.PrecoBase;
import mobi.audax.tupi.passageiro.bin.dto.ViagemMotorista;
import mobi.audax.tupi.passageiro.bin.task.location.LocationCommons;
import mobi.audax.tupi.passageiro.bin.task.location.LocationThread;
import mobi.audax.tupi.passageiro.bin.util.MaskTelefone;
import mobi.audax.tupi.passageiro.bin.util.Notification;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.SNotification;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class NavigationActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "NavigationActivity";
    private static final int MENOR_DISTANCIA_PARADA = 20;
    private static final int MAIOR_DISTANCIA_PARADA = 50;
    private final List<MapMarker> mapMarkerMotoristaIconList = new ArrayList<>();
    int LAUNCH_LOGIN_ACTIVITY = 1;
    int LAUNCH_PAGAMENTO_ACTIVITY = 2;
    private boolean showDialogTupiChegou = false;
    private boolean showDialogMotoristaProximo = false;
    private boolean showDialogPerdeuTupi = false;
    private boolean showDialogFimDaViagem = false;
    private boolean showDialogPontoParada = false;
    private ProgressDialog progressDialog;
    private Passenger passenger;
    private PrecoBase precoBase;
    private Viagem viagem;
    private Timer timer;
    private List<PassageiroNaoCadastrado> pncs;
    private MapView mapView;
    private LinearLayout llDeslocamento, llEmNavegacao, llDadosMotorista;
    private TextView mTvTempoDeslocamento;
    private TextView mTvToolbar, mTvIndoPara, mTvKmDeslocamento, mTvRuaEmDeslocamento, mTvKmAteDestino, mTvTempoAteDestino;
    private int id = 1000;
    private int idNomePassageiro = 2000;
    private int idCpfPassageiro = 3000;
    private LinearLayout llMaisPassageiros;
    private List<Integer> listPassageiros;
    private ImageView mIvZoom, ivCancelarCorrida, ivCancelarCorridaBottom;
    private SearchEngine searchEngine;
    private RoutingUtils routingUtils;
    private ViagemMotorista viagemMotorista;
    private boolean paraMim = true;
    private boolean zoomAtivo = false;

    private BroadcastReceiver receiver = null;
    private BroadcastReceiver receiverChegou = null;
    private BroadcastReceiver receiverViagemCancelada = null;
    private BroadcastReceiver receiverPix = null;
    private LocationIndicator locationIndicator;
    private SNotification notification;
    private TextView labelPagemento;
    private Boolean initRotaSucess = false;
    private Pagamento pagamentoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        passenger = new PassengerBo(this).autenticado();

        initViews();
        var prefs = new Prefs(this);
        mapView.onCreate(savedInstanceState);
        routingUtils = new RoutingUtils(NavigationActivity.this, mapView);
        setTapGestureHandler();

        loadMapScene();

        receberBroadcast();
        receberChegouBroadcast();
        receberViagemCanceladaBroadcast();
        receberNotificacaoPix();

        this.atualizarLocalizacao();
        mIvZoom.setOnClickListener(view -> {
            //     Log.e(TAG, "onCreate: " + mapView.getCamera().getState().toString() );
            //   if (!zoomAtivo) {
            //     zoomAtivo = true;
            //   Log.e(TAG, "onCreate: zomm 14 "  );
            // mapView.getCamera().zoomTo(15);
            //       } else {
            //         zoomAtivo = false;
            //       Log.e(TAG, "onCreate: zomm 199999 "  );
            //     mapView.getCamera().zoomTo(19);
            //    }
        });

        mIvZoom.setOnClickListener(view -> {
            final var vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

            var controller = new LocationThread(this, location -> {
                this.updateLocationIndicator(location);
                mapView.getCamera().lookAt(new GeoCoordinates(location.getLatitude(), location.getLongitude()), 1000);
                //  mapView.getCamera().flyTo(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
                return null;
            });
            controller.setUseOnlyLastKnowLocation(true);
            controller.requestLocation();
        });

        ivCancelarCorrida.setOnClickListener(view -> showDialogCancelarCorrida());
        ivCancelarCorridaBottom.setOnClickListener(view -> {
            // showDialogCancelarCorrida()
        });

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                watchMotorista();
            }
        }, 0, 1000 * 10);

        if (prefs.getEmbarcou()) {
            Log.e("NVI EMBAR", "onCreate: getEmbarcou >>>>>>>>>> ");
            this.iniciarNavegacacao();
        }

    }

    private void iniciarNavegacacao() {
        llEmNavegacao.setVisibility(View.VISIBLE);
        ivCancelarCorrida.setVisibility(View.GONE);
        ivCancelarCorridaBottom.setVisibility(View.INVISIBLE);
        llDadosMotorista.setVisibility(View.GONE);
        llDeslocamento.setVisibility(View.GONE);

        this.showDialogPerdeuTupi = true;
        this.showDialogTupiChegou = true;
        this.showDialogMotoristaProximo = true;
    }

    private void watchMotorista() {
        if (!new Prefs(this).getEmbarcou()) {
            if (new ViagemBo(this).current() != null) {
                tempoMotoristaParada();
            }
        } else {
            timer.cancel();
        }
    }

    private void initViews() {
        //shared latitude atual do passageiro

        locationIndicator = new LocationIndicator();
        locationIndicator.setLocationIndicatorStyle(PEDESTRIAN);
        pncs = new ArrayList<>();

        Notification navigation = new Notification();
        notification = new SNotification(this);
        Intent intentNavigation = new Intent(this, NavigationActivity.class);

        viagem = (Viagem) getIntent().getSerializableExtra("VIAGEM");
        viagemMotorista = (ViagemMotorista) getIntent().getSerializableExtra("VIAGEMMOTORISTA");

        //toolbar
        var myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        //   getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //views
        ImageView mImCuurenteLoc = findViewById(R.id.ivCurrentLocNavigation);
        ivCancelarCorridaBottom = findViewById(R.id.imageView9);
        ivCancelarCorrida = findViewById(R.id.imageViewCancelarCorrida);
        ImageView ivFotoMotorista = findViewById(R.id.iv_foto_motorista);
        TextView mTvNomeMotorista = findViewById(R.id.tv_nome_motorista);
        TextView mTvModelo = findViewById(R.id.tv_modelo_veiculo);
        TextView mTvPlaca = findViewById(R.id.tv_placa_veiculo);
        mTvKmDeslocamento = findViewById(R.id.textViewDistanciaEmKmDeslocamento);
        mTvTempoDeslocamento = findViewById(R.id.textViewTempoDeslocamentoDestino);
        mTvRuaEmDeslocamento = findViewById(R.id.textViewRuaDeslocamento);
        mTvKmAteDestino = findViewById(R.id.textViewKmAteODestino);
        mTvTempoAteDestino = findViewById(R.id.textViewTempoAteODestino);
        llDeslocamento = findViewById(R.id.llEmDeslocamento);
        llEmNavegacao = findViewById(R.id.llEmNavegacao);
        llDadosMotorista = findViewById(R.id.llDadosMotorista);
        mTvToolbar = findViewById(R.id.textViewLocalToolbar);
        mTvToolbar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTvToolbar.setSelected(true);
        mIvZoom = findViewById(R.id.imageViewZoom);
        mTvIndoPara = findViewById(R.id.textView);
        mapView = findViewById(R.id.map_View3);
        mTvToolbar.setText("Info. da viagem");

        if (viagem != null) {

            //Spanned spanned = HtmlCompat.fromHtml("<b>Motorista: </b>", HtmlCompat.FROM_HTML_MODE_LEGACY);
            mTvNomeMotorista.setText(viagem.getMotorista());
            mTvModelo.setText(viagem.getModelo());
            mTvPlaca.setText(MaskTelefone.addMaskPlaca(viagem.getPlaca(), "###-####"));
            if (viagem.getFotoMotorista() != null) {
                var path = viagem.getFotoMotorista();
                Picasso.get().load(path).into(ivFotoMotorista);
            }
        }

    }

    private void receberBroadcast() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                var notification = new SNotification(NavigationActivity.this);

                Passageiro passageiro = (Passageiro) intent.getSerializableExtra("PASSAGEIRO");

                if (passageiro != null) {
                    Log.e(TAG, "receberBroadcast isEmbarcou : " + passageiro.isEmbarcou());
                    if (passageiro.isEmbarcou()) {
                        var prefs = new Prefs(NavigationActivity.this);
                        prefs.setEmbarcou(true);
                        //  notification.sendNotification(getString(R.string.app_name), getString(R.string.voce_embarcou_tupi), null, null, 1);
                        showDialogEmbarcouTupi();
                    } else {
                        notification.sendNotification(getString(R.string.app_name), "Você perdeu o tupi...", null, null, 1);
                        if (timer != null) {
                            timer.cancel();
                            showDialogPerdeuTupi();
                        }
                    }
                }
            }
        };

        var filter = new IntentFilter();
        filter.addAction("MOTORISTA-PUSH");
        registerReceiver(receiver, filter);
    }

    private void receberChegouBroadcast() {
        receiverChegou = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                var prefs = new Prefs(NavigationActivity.this);
                if (!prefs.getDesembarcou()) {
                    prefs.setDesembarcou(true);
                    showDialogFimDaViagem(2);
                }


            }
        };

        var filter = new IntentFilter();
        filter.addAction("MOTORISTA-CHEGOU");
        registerReceiver(receiverChegou, filter);
    }

    private void receberViagemCanceladaBroadcast() {
        receiverViagemCancelada = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //   navigation.showNotification(NavigationActivity.this,"Você embarcou no Tupi" , "Boa viagem!", intentNavigation);
                showDialogViagemCancelada();
            }
        };

        var filter = new IntentFilter();
        filter.addAction("VIAGEM_CANCELADA");
        registerReceiver(receiverViagemCancelada, filter);
    }

    private void receberNotificacaoPix() {
        Log.e("FCM", "pix aprovado 2");
        receiverPix = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //   navigation.showNotification(NavigationActivity.this,"Você embarcou no Tupi" , "Boa viagem!", intentNavigation);
                showPix();
                Log.e("FCM", "pix aprovado 4");
            }
        };

        var filter = new IntentFilter();
        filter.addAction("PIX_APROVADO");
        registerReceiver(receiverPix, filter);
    }

    private void showDialogCancelarCorrida() {
        if (!this.isFinishing()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            final View customLayout = getLayoutInflater().inflate(R.layout.dialog_cancelar_corrida, null);
            MaterialButton mBtnContinuarViagem = customLayout.findViewById(R.id.dialogButton);
            MaterialButton mBtnCancelarViagem = customLayout.findViewById(R.id.dialogButton2);
            dialog.setView(customLayout);
            final AlertDialog alertDialog = dialog.create();
            mBtnContinuarViagem.setOnClickListener(view -> alertDialog.dismiss());
            mBtnCancelarViagem.setOnClickListener(view -> {
                alertDialog.dismiss();
                cancelarViagem();
            });

            alertDialog.show();
        }
    }

    private void showDialogViagemCancelada() {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_viagem_cancelada, null);
            var mBtnContinuarViagem = (MaterialButton) customLayout.findViewById(R.id.dialogButton);
            dialog.setView(customLayout);
            final var alertDialog = dialog.create();
            mBtnContinuarViagem.setOnClickListener(view -> {
                alertDialog.dismiss();
                goToHome();
            });

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void showPix() {
        Log.e("FCM", "pix aprovado 5");
//        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_pix, null);
            var mBtnContinuarViagem = (MaterialButton) customLayout.findViewById(R.id.dialogButton);
            dialog.setView(customLayout);
            final var alertDialog = dialog.create();
            mBtnContinuarViagem.setOnClickListener(view -> {
                alertDialog.dismiss();
                goToHome();
            });

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
//    }

    private void showDialogPontoParada() {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_ponto_de_parada_chegou, null);
            var mBtnContinuarViagem = (MaterialButton) customLayout.findViewById(R.id.dialogButton);
            dialog.setView(customLayout);
            final var alertDialog = dialog.create();
            mBtnContinuarViagem.setOnClickListener(view -> alertDialog.dismiss());
            alertDialog.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void showDialogFimDaViagem(int i) {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_fim_viagem, null);
            var mBtnOk = customLayout.findViewById(R.id.dialogButton2);
            dialog.setView(customLayout);
            final var alertDialog = dialog.create();
            alertDialog.setCanceledOnTouchOutside(false);
            mBtnOk.setOnClickListener(view -> {
                if (i == 1) {
                    encerrarViagem();
                } else {
                    goToHome();
                }
                alertDialog.dismiss();

            });
            alertDialog.create();
            alertDialog.show();
        }
    }

    private void clearWaypointMotoristaMarker() {
        for (var mapMarker : mapMarkerMotoristaIconList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerMotoristaIconList.clear();
    }

    public void addMotoristaMarker(GeoCoordinates geoCoordinates, int resourceId) {
        var mapImage = MapImageFactory.fromResource(this.getResources(), resourceId);
        var anchor = new Anchor2D(0.5f, 1.0f);
        var mapMarker = new MapMarker(geoCoordinates, mapImage, anchor);
        clearWaypointMotoristaMarker();
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerMotoristaIconList.add(mapMarker);
    }

    private boolean iniciarRota() {
        initRotaSucess = false;
        var prefs = new Prefs(this);

        var confirmarViagemv = new ConfirmarViagem();
        confirmarViagemv.setParaMim(paraMim);
        confirmarViagemv.setPassageiro(passenger.getId());
        confirmarViagemv.setPush(prefs.getToken());
        confirmarViagemv.setViagem(viagem.getViagem());
        confirmarViagemv.setParadaEmbarque(viagem.getParadaEmbarque());
        confirmarViagemv.setParadaDesembarque(viagem.getParadaDesembarque());
        confirmarViagemv.setPassageiroNaoCadastrados(pncs);
        confirmarViagemv.setValorPassagem(precoBase.getPrecoViagem() * (pncs.size() + 1));

        var controller = new RotaPassageiroController(this);
        controller.confirmarViagem(confirmarViagemv, () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.solicitando_viagem), true, true);
            return null;
        }, statusCode -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            switch (statusCode) {
                case 200 -> {
                    var viagemBo = new ViagemBo(this);

                    viagemBo.insert(viagem);

                    var prefss = new Prefs(this);
                    if (prefss.getEmbarcou()) {
                        this.iniciarNavegacacao();
                    } else {
                        llDeslocamento.setVisibility(View.VISIBLE);
                        llDadosMotorista.setVisibility(View.VISIBLE);
                        ivCancelarCorrida.setVisibility(View.VISIBLE);
                    }
                    initRotaSucess = true;
                }
                case 402 ->
                        Util.showAlertDialog(getString(R.string.atencao), getString(R.string.pagamento_nao_autorizado_verifique_seu_cartao_credito), this, () -> {});
                default ->
                        Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this, this::finish);
            }
            return null;
        });
        return initRotaSucess;
    }

    private boolean verificarExistenciaCartaoPagamento() {
        boolean hasCartao = true;
        CartaoBo cartaoBo = new CartaoBo(this);
        List<Cartao> listCartoes = cartaoBo.list();
        if (listCartoes.isEmpty()) {
            hasCartao = false;
            showMessageSemCartao();
        } else {
            var pagamentos = new ArrayList<Pagamento>();
            List<Pagamento> pagamentosCartoes = getCartoesForPagamento(listCartoes, pagamentos);
            if (pagamentosCartoes.isEmpty()) {
                hasCartao = false;
                showMessageSemCartao();
            }
        }
        return hasCartao;
    }

    @SuppressLint("MissingPermission")
    private void atualizarLocalizacao() {
        var commons = new LocationCommons();
        if (commons.isLocationEnabled(this)) {
            var locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, NavigationActivity.this);
        } else {
            Toast.makeText(this, R.string.por_favor_ative_sua_localizacao, Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private void showMessageSemCartao() {
        Util.alert(this, R.string.cartoes_cadastrado, R.string.cartoes_nao_encontrados_para_pagamento, R.string.cadastrar_cartao, () -> {
            Intent intent = new Intent(this, AdicionarNovoCartaoActivity.class);
            startActivityForResult(intent, LAUNCH_LOGIN_ACTIVITY);
        }, R.string.ok);
    }

    private List<Pagamento> getCartoesForPagamento(List<Cartao> listCartoes, ArrayList<Pagamento> pagamentos) {
        return listCartoes.stream()
                .filter(this::cartaoIsValid)
                .flatMap(cartao -> {
                    pagamentos.add(new Pagamento(cartao));
                    return pagamentos.stream();
                }).collect(Collectors.toList());
    }

    private boolean cartaoIsValid(Cartao cartao) {
        return cartao.getId() > 0
                && !Objects.requireNonNull(cartao.getNumero()).isBlank()
                && !Objects.requireNonNull(cartao.getCvv()).isBlank();
    }

    private void precoBase() {
        precoBase = new PrecoBase();
        if (viagem != null && viagem.getValorPassagem() > 0) {
            precoBase.setPrecoViagem(viagem.getValorPassagem());
            showBottomSheetDialog();
        } else {
            var controller = new RotaPassageiroController(this);
            controller.precoBase((statusCode, precoBase) -> {
                this.precoBase = precoBase;
                if (statusCode == 200) {
                    showBottomSheetDialog();
                } else {
                    Util.showAlertDialog(getString(R.string.atencao), getString(R.string.erro_obter_valores_viagem), this, this::finish);
                }

                return null;
            });
        }
    }

    private void tempoMotoristaParada() {
        var pvr = new PosicaoViagemRequest();
        if (viagem != null) {
            pvr.setViagem(viagem.getViagem());
            pvr.setParada(viagem.getParadaEmbarque());
        } else {
            Viagem viagem = new ViagemBo(NavigationActivity.this).current();
            pvr.setViagem(viagem.getViagem());
            pvr.setParada(viagem.getParadaEmbarque());
        }

        var controller = new RotaPassageiroController(this);
        controller.tempoMotoristaParada(pvr, (statusCode, paradaEmbarque) -> {

            switch (statusCode) {
                case 200 -> {
                    var prefs = new Prefs(this);
                    var tempo = paradaEmbarque.getTempoMotoristaParada();
                    var lat = paradaEmbarque.getPosicaoMotorista().getLatitude();
                    var lng = paradaEmbarque.getPosicaoMotorista().getLongitude();
                    addMotoristaMarker(new GeoCoordinates(lat, lng), R.drawable.ic_carro_motorista);

                    routingUtils.addRoutePassageiro(viagem, prefs.getLatitude(), prefs.getLongitude(), mTvKmDeslocamento, mTvTempoDeslocamento);
                    routingUtils.addRoutePassageiroDetails(viagem, lat, lng, mTvKmAteDestino, mTvTempoAteDestino);
                    //        var geoCoordinates = new GeoCoordinates(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque());
                    //        addPassageiroDestinoMarker(geoCoordinates, R.drawable.icone_gps_01);

                    var distanciaAteDestino = Util.haversine(lat, lng, viagem.getLatitudeEmbarque(), viagem.getLongitudeEmbarque());

                    if (distanciaAteDestino < MENOR_DISTANCIA_PARADA || tempo < 20) {
                        if (!this.showDialogTupiChegou) {
                            notification.sendNotification("Seu TUPI chegou!", "Você tem 30 segundos para embarcar...", null, null, 1);
                            showDialogTupiChegou();
                            this.showDialogTupiChegou = true;
                        }
                    } else if (distanciaAteDestino < MAIOR_DISTANCIA_PARADA || tempo < 60 && tempo > 20) {
                        if (!this.showDialogMotoristaProximo) {
                            notification.sendNotification("Tupi", "Seu TUPI está proximo!", null, null, 1);
                            showMotoristaDialog(tempo);
                            this.showDialogMotoristaProximo = true;
                        }
                    }
                }
                case 410 -> {
                    if (!new Prefs(this).getEmbarcou()) {
                        if (!this.showDialogPerdeuTupi) {
                            goToHome();
                            this.showDialogPerdeuTupi = true;
                        }
                    }
                }
                default -> {
                }
            }
            return null;
        });
    }

    private void showDialogPerdeuTupi() {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_perdeu_viagem, null);
            var mBtnAlertNovaViagem = (MaterialButton) customLayout.findViewById(R.id.dialogButton);
            var ivSair = (ImageView) customLayout.findViewById(R.id.sair_dialog);

            dialog.setView(customLayout);
            final AlertDialog alertDialog = dialog.create();
            ivSair.setOnClickListener(v -> alertDialog.dismiss());
            mBtnAlertNovaViagem.setOnClickListener(v -> {
                alertDialog.dismiss();
                goToHome();
            });
            ivSair.setOnClickListener(view -> {
                alertDialog.dismiss();
                goToHome();
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.create();
            alertDialog.show();
        }
    }

    private void showMotoristaDialog(double tempo) {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_tempo_motorista, null);
            TextView mTextViewAlert = customLayout.findViewById(R.id.dialogTextView2);
            MaterialButton mBtnAlert = customLayout.findViewById(R.id.dialogButton);
            dialog.setView(customLayout);
            final AlertDialog alertDialog = dialog.create();
            mBtnAlert.setOnClickListener(v -> alertDialog.dismiss());

            mTextViewAlert.setTextColor(Color.parseColor("#CB4040"));
            String fmt = String.valueOf(tempo);
            String textoSeparado = fmt.substring(0, 2);
            mTextViewAlert.setText(textoSeparado + " segundos");

            mBtnAlert.setTextColor(Color.parseColor("#CB4040"));
            mBtnAlert.setOnClickListener(v -> alertDialog.dismiss());

            alertDialog.create();
            alertDialog.show();
        }
    }

    private void showDialogTupiChegou() {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_tupi_chegou, null);
            var mBtnAlert = customLayout.findViewById(R.id.dialogButton);
            var message = (TextView) customLayout.findViewById(R.id.message);
            message.setText(HtmlCompat.fromHtml(getString(R.string.seu_tupi_chegou_voce_tem_30_segundos_para_embarcar), HtmlCompat.FROM_HTML_MODE_COMPACT));
            dialog.setView(customLayout);
            final AlertDialog alertDialog = dialog.create();
            mBtnAlert.setOnClickListener(v -> alertDialog.dismiss());

            alertDialog.create();
            alertDialog.show();
        }
    }

    private void showDialogEmbarcouTupi() {
        if (!this.isFinishing()) {
            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_embarcou_tupi, null);
            var mBtnAlert = (MaterialButton) customLayout.findViewById(R.id.dialogButton);
            dialog.setView(customLayout);
            final var alertDialog = dialog.create();
            mBtnAlert.setOnClickListener(v -> alertDialog.dismiss());

            alertDialog.create();
            alertDialog.show();
        } else {
            var notification = new SNotification(this);
            notification.sendNotification(getString(R.string.app_name), getString(R.string.voce_embarcou_tupi), null, null, 1);
        }

        clearWaypointMotoristaMarker();
        routingUtils.addRoute(viagemMotorista);
        var geoCoordinates = new GeoCoordinates(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque());
        addPassageiroDestinoMarker(geoCoordinates, R.drawable.icone_gps_01, viagem.getNomeParadaDesembarque());

        var prefs = new Prefs(this);
        prefs.setEmbarcou(true);

        this.iniciarNavegacacao();

    }

    @Override
    public void onBackPressed() {
        //     showDialogVoltar();
        //   Toast.makeText(this, "Aguarde sua corrida ser finalizada!!", Toast.LENGTH_LONG).show();
    }

    private void showDialogVoltar() {
        var dialog = new AlertDialog.Builder(this);
        var customLayout = getLayoutInflater().inflate(R.layout.dialog_sair_viagem, null);
        var mBtnContinuar = customLayout.findViewById(R.id.dialogButton);
        var mBtnSair = customLayout.findViewById(R.id.dialogButton2);
        dialog.setView(customLayout);
        final var alertDialog = dialog.create();

        mBtnContinuar.setOnClickListener(v -> alertDialog.dismiss());
        mBtnSair.setOnClickListener(v -> {
            alertDialog.dismiss();
            cancelarViagem();
        });
        alertDialog.show();

//        llEmNavegacao.setVisibility(View.VISIBLE);
//        llDeslocamento.setVisibility(View.GONE);

    }

    private void cancelarViagem() {
        var controller = new RotaPassageiroController(this);
        controller.cancelarViagem(() -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.cancelando), true);
            return null;
        }, statusCode -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (statusCode == 200 || statusCode == 409) {
                goToHome();
            } else {
                Util.showAlertDialog(getString(R.string.atencao), getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    private void goToHome() {
        new ViagemBo(this).clean();
        finish();
    }

    private void encerrarViagem() {
        var finalizar = new FinalizarViagem();
        finalizar.setViagem(viagem.getViagem());
        finalizar.setPassageiro(passenger.getId());
        finalizar.setParadaDesembarque(viagem.getParadaDesembarque());

        var controller = new RotaPassageiroController(this);
        controller.finalizar(finalizar, () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.finalizando), true);
            return null;
        }, (statusCode) -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (statusCode == 200) {
                goToHome();
            } else if (statusCode == 409) {
                if (!new Prefs(this).getEmbarcou()) {
                    goToHome();
                } else {
                    Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_finalizar_viagem), this, this::goToHome);
                }
            } else {
                Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this, this::goToHome);
            }
            return null;
        });
    }

    private void loadMapScene() {
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, mapError -> {
            if (mapError == null) {
                var prefs = new Prefs(this);

                mapView.getCamera().lookAt(new GeoCoordinates(prefs.getLatitude(), prefs.getLongitude()), 1000);
                mapView.addLifecycleListener(locationIndicator);

                var location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(prefs.getLatitude());
                location.setLongitude(prefs.getLongitude());
                location.setAccuracy(prefs.getPrecisao());
                location.setBearing(prefs.getBearing());
                this.updateLocationIndicator(location);

                Log.e(TAG, "loadMapScene: addRoute");

                routingUtils.addRoute(viagemMotorista);

                if (new ViagemBo(this).current() != null) {
                    if (!prefs.getEmbarcou()) {
                        ivCancelarCorrida.setVisibility(View.VISIBLE);
                        llDadosMotorista.setVisibility(View.VISIBLE);
                        llDeslocamento.setVisibility(View.VISIBLE);
                        routingUtils.addRoutePassageiro(viagem, prefs.getLatitude(), prefs.getLongitude(), mTvKmDeslocamento, mTvTempoDeslocamento);
                    } else {
                        routingUtils.addRoutePassageiroDetails(viagem, location.getLatitude(), location.getLongitude(), mTvKmAteDestino, mTvTempoAteDestino);
                        llEmNavegacao.setVisibility(View.VISIBLE);
                        var geoCoordinates = new GeoCoordinates(viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque());
                        addPassageiroDestinoMarker(geoCoordinates, R.drawable.icone_gps_01, viagem.getNomeParadaDesembarque());
                    }

                } else {
                    precoBase();
                }

            } else {
                Log.d(TAG, "onLoadScene failed: " + mapError);
            }
        });
    }

    private void updateLocationIndicator(@NonNull Location location) {
        var data = new com.here.sdk.core.Location.Builder()
                .setCoordinates(new GeoCoordinates(location.getLatitude(), location.getLongitude()))
                .setTimestamp(new Date())
                .setBearingInDegrees((double) location.getBearing())
                .build();

        locationIndicator.updateLocation(data);
    }

    private void showBottomSheetDialog() {
        final var bsdConfirmarPassageiros = new BottomSheetDialog(this);
        bsdConfirmarPassageiros.setContentView(R.layout.bottom_sheet_confirmar_passageiros);
        var decimalFormat = new DecimalFormat("0.00");

        TextView tvMaisPassageiros = bsdConfirmarPassageiros.findViewById(R.id.textViewMaisPassageiros);
        CheckBox checkBoxNaoSereiOPassageiro = bsdConfirmarPassageiros.findViewById(R.id.checkBox);
        TextView tvMenosPassageiros = bsdConfirmarPassageiros.findViewById(R.id.textViewMenosPassageiros);
        TextView tvNumeroPassageiros = bsdConfirmarPassageiros.findViewById(R.id.textViewNumeroPassageiros);
        TextView tvPrecoDaViagem = bsdConfirmarPassageiros.findViewById(R.id.textViewPrecoViagem);
        llMaisPassageiros = bsdConfirmarPassageiros.findViewById(R.id.llEmbarcarMaisPassageiros);
        Button mBtnConfirmarViagem = bsdConfirmarPassageiros.findViewById(R.id.buttonConfirmarViagem);
        Button mBtnCancelarViagem = bsdConfirmarPassageiros.findViewById(R.id.buttonCancelarViagem);
        View layoutMetodoPagamento = bsdConfirmarPassageiros.findViewById(R.id.layoutMetodoPagamento);
        labelPagemento = bsdConfirmarPassageiros.findViewById(R.id.labelMetodoPagamento);

        layoutMetodoPagamento.setOnClickListener((view -> {
            if (passenger != null) {
                Intent intent = new Intent(this, SelecionarMetodoPagamentoActivity.class);
                intent.putExtra("valor", tvPrecoDaViagem.getText().toString());
                startActivityForResult(intent, LAUNCH_PAGAMENTO_ACTIVITY);
            } else {
                showMessageNaoAutenticado();
            }
        }));

        listPassageiros = new ArrayList<>();

        checkBoxNaoSereiOPassageiro.setOnCheckedChangeListener((compoundButton, b) -> paraMim = !b);
        mBtnCancelarViagem.setOnClickListener(view1 -> {
            if (bsdConfirmarPassageiros.isShowing()) {
                bsdConfirmarPassageiros.dismiss();
            }
            finish();
        });

        tvPrecoDaViagem.setText(decimalFormat.format(precoBase.getPrecoViagem()));
        tvNumeroPassageiros.setText(String.valueOf(1));

        tvMaisPassageiros.setOnClickListener(view -> {
            criarLayoutProgramaticamente();

            int passageiros = Integer.parseInt(tvNumeroPassageiros.getText().toString());
            passageiros++;
            tvNumeroPassageiros.setText(String.valueOf(passageiros));
            tvPrecoDaViagem.setText(decimalFormat.format(precoBase.getPrecoViagem() * passageiros));
        });

        tvMenosPassageiros.setOnClickListener(view -> {
            int passageiros = parseInt(tvNumeroPassageiros.getText().toString());
            if (passageiros > 1) {
                if (!listPassageiros.isEmpty()) {
                    int idLinear = listPassageiros.get(listPassageiros.size() - 1);
                    LinearLayout llRemoverLayout = bsdConfirmarPassageiros.findViewById(idLinear);
                    llMaisPassageiros.removeView(llRemoverLayout);
                    listPassageiros.remove(listPassageiros.size() - 1);
                    id--;
                    idNomePassageiro--;
                    idCpfPassageiro--;
                }
                passageiros--;
                tvNumeroPassageiros.setText(String.valueOf(passageiros));
                tvPrecoDaViagem.setText(decimalFormat.format(precoBase.getPrecoViagem() * passageiros));
            } else {
                Toast.makeText(this, "Mínimo de 1 passageiro por viagem.", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnConfirmarViagem.setOnClickListener(view -> {
            if (passenger != null) {
                if (id > 1000) {
                    for (int i = 1001; i <= id; i++) {
                        var passageiro = new PassageiroNaoCadastrado();
                        EditText etNomePassageiro;
                        EditText etCpfPassageiro;
                        int idNome = i + 1000;
                        int idCpf = i + 2000;

                        etNomePassageiro = bsdConfirmarPassageiros.findViewById(idNome);
                        etCpfPassageiro = bsdConfirmarPassageiros.findViewById(idCpf);
                        passageiro.setNome(etNomePassageiro.getText().toString());
                        passageiro.setDocumento(etCpfPassageiro.getText().toString());
                        pncs.add(passageiro);
                    }
                }

//                boolean hasCartao = verificarExistenciaCartaoPagamento();
//                if (hasCartao) {
                    boolean sucess = iniciarRota();
                    if (sucess) {
                        bsdConfirmarPassageiros.dismiss();
                        mTvIndoPara.setText(R.string.indo_para);
                        mTvToolbar.setText(viagem.getNome());
                    }
//                }
            } else {
                showMessageNaoAutenticado();
            }
        });
        bsdConfirmarPassageiros.setCanceledOnTouchOutside(false);
        bsdConfirmarPassageiros.setCancelable(false);
        bsdConfirmarPassageiros.show();
    }

    private void criarLayoutProgramaticamente() {
        LinearLayout linear = new LinearLayout(this);
        TextView textView = new TextView(this);
        TextView textView2 = new TextView(this);
        EditText myEditText = new EditText(this);
        EditText myEditText2 = new EditText(this);

        id++;
        listPassageiros.add(id);
        linear.setId(id);
        linear.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins(16, 8, 8, 8);
        linear.setLayoutParams(layoutParams2);

        Typeface face = ResourcesCompat.getFont(NavigationActivity.this, R.font.ubuntu_bold);
        textView.setTypeface(face);
        textView.setText(R.string.nome_do_passageiro);
        textView.setTextColor(Color.parseColor("#565656"));

        textView2.setTypeface(face);
        textView2.setText(R.string.cpf);
        textView2.setTextColor(Color.parseColor("#565656"));

        myEditText.setBackgroundResource(R.drawable.ic_etbg);
        myEditText.setTypeface(face);
        idNomePassageiro++;
        myEditText.setId(idNomePassageiro);
        Log.e(TAG, "criarLayoutProgramaticamente: myeditText set id " + idNomePassageiro);
        myEditText.setHint(R.string.nome_do_passageiro_que_ir_embarcar);
        myEditText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);

        myEditText2.setBackgroundResource(R.drawable.ic_etbg);
        myEditText2.setTypeface(face);
        idCpfPassageiro++;
        myEditText2.setId(idCpfPassageiro);

        myEditText2.setHint(R.string.digite_o_cpf_do_passageiro);
        myEditText2.setInputType(InputType.TYPE_CLASS_NUMBER);
        int maxLength = 11;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        myEditText2.setFilters(fArray);

        linear.addView(textView);
        linear.addView(myEditText);
        linear.addView(textView2);
        linear.addView(myEditText2);

        llMaisPassageiros.addView(linear);
    }

    public void addPassageiroDestinoMarker(GeoCoordinates geoCoordinates, int resourceId, String nomeParada) {
        var mapImage = MapImageFactory.fromResource(this.getResources(), resourceId);
        var anchor = new Anchor2D(0.5f, 1.0f);
        var mapMarker = new MapMarker(geoCoordinates, mapImage, anchor);

        var metaData = new Metadata();
        if (nomeParada != null) {
            metaData.setString("nome", nomeParada);
            mapMarker.setMetadata(metaData);
        }
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    //teste de click
    private void setTapGestureHandler() {
        mapView.getGestures().setTapListener(this::pickMapMarker);
    }

    private void pickMapMarker(final Point2D touchPoint) {
        var radiusInPixel = 100f;
        mapView.pickMapItems(touchPoint, radiusInPixel, pickMapItemsResult -> {
            if (pickMapItemsResult == null) {
                return;
            }

            var mapMarkerList = pickMapItemsResult.getMarkers();
            if (mapMarkerList.size() == 0) {
                return;
            }

            var marker = mapMarkerList.get(0);
            var metadata = marker.getMetadata();
            if (metadata != null) {
                //     var id = metadata.getInteger("id");
                var nome = metadata.getString("nome");
                //    if (id != null) {
                Toast.makeText(NavigationActivity.this, "Destino: " + nome, Toast.LENGTH_LONG).show();
                //   }
            }
        });
    }

    //fim de teste click
    private void getAddressForCoordinates(GeoCoordinates geoCoordinates) {
        var maxItems = 1;
        var reverseGeocodingOptions = new SearchOptions(LanguageCode.EN_GB, maxItems);
        try {
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            e.printStackTrace();
        }
        searchEngine.search(geoCoordinates, reverseGeocodingOptions, (searchError, list) -> {
            if (searchError != null) {
                Toast.makeText(NavigationActivity.this, "" + searchError, Toast.LENGTH_SHORT).show();
                return;
            }
            mTvRuaEmDeslocamento.setText(list.get(0).getAddress().street);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(receiverChegou);
        unregisterReceiver(receiverViagemCancelada);
        this.timer.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //   showDialogVoltar();
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double distanceInMeters = 1000;
        var orientation = new GeoOrientationUpdate((double) location.getBearing(), 45.0);
        mapView.getCamera().lookAt(new GeoCoordinates(location.getLatitude(), location.getLongitude()), orientation, distanceInMeters);
        //  clearWaypointMotoristaMarker();
        if (new Prefs(this).getEmbarcou()) {
            var distanciaAteDestino = Util.haversine(location.getLatitude(), location.getLongitude(), viagem.getLatitudeDesembarque(), viagem.getLongitudeDesembarque());
            Log.e(TAG, "onLocationChanged: distanciaAteDestino " + distanciaAteDestino);

            if (distanciaAteDestino < MENOR_DISTANCIA_PARADA) {
                notification.sendNotification(getString(R.string.app_name), "Você chegou ao fim da viagem", null, null, 1);
                var prefs = new Prefs(NavigationActivity.this);
                Log.e(TAG, "onLocationChanged: showDialogFimDaViagem ");
                if (!prefs.getDesembarcou()) {
                    prefs.setDesembarcou(true);
                    showDialogFimDaViagem(1);
                }
            } else if (distanciaAteDestino < MAIOR_DISTANCIA_PARADA) {
                if (!this.showDialogPontoParada) {
                    showDialogPontoParada();
                    showDialogPontoParada = true;
                }
            }
        }

        getAddressForCoordinates(new GeoCoordinates(location.getLatitude(), location.getLongitude()));

        this.updateLocationIndicator(location);
        routingUtils.addRoutePassageiroDetails(viagem, location.getLatitude(), location.getLongitude(), mTvKmAteDestino, mTvTempoAteDestino);
        var prefs = new Prefs(this);
        prefs.recordLocation(location);
    }

    public void onCurrentLocation(View view) {
        final var vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

        var controller = new LocationThread(this, location -> {
            this.updateLocationIndicator(location);
            mapView.getCamera().flyTo(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
            return null;
        });
        controller.setUseOnlyLastKnowLocation(true);
        controller.requestLocation();
    }

    private void showMessageNaoAutenticado() {
        Util.alert(this, R.string.usuario_nao_autenticado, R.string.necessario_autenticacao, R.string.entrar_ou_criar_conta, () -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LAUNCH_LOGIN_ACTIVITY);
        }, R.string.ok);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_LOGIN_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                boolean result = data.getBooleanExtra("isLoggedIn", false);
                if (result) {
                    Passenger autenticado = new PassengerBo(this).autenticado();
                    if (autenticado != null) {
                        passenger = autenticado;
                    }
                }
            }
        } else if (requestCode == LAUNCH_PAGAMENTO_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra("pagamentoSelecionado")) {
                    String pagamentoJson = data.getStringExtra("pagamentoSelecionado");
                    pagamentoSelecionado = new Gson().fromJson(pagamentoJson, Pagamento.class);
                    if (pagamentoSelecionado != null) {
                        labelPagemento.setText(pagamentoSelecionado.getLabel());
                    }
                }
            }
        }
    }

}