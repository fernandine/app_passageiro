package mobi.audax.tupi.passageiro.activities.home.menulateral.historico;

import static mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum.ACIDENTE;
import static mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum.ALGO_DIFERENTE;
import static mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum.ASSALTO;
import static mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum.CLIENTE_NAO_SUBIU;
import static mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum.ITENS_PERDIDOS;
import static mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum.VEICULO_QUEBROU;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.Passageiro;
import mobi.audax.tupi.passageiro.bin.bo.PassageiroBo;
import mobi.audax.tupi.passageiro.bin.dto.MinhaRota;
import mobi.audax.tupi.passageiro.bin.util.Util;
import mobi.stos.httplib.HttpAsync;
import mobi.stos.httplib.inter.FutureCallback;

public class HistoricoViagensDetalheActivity extends AppCompatActivity {
    private static final String TAG = "HistoricoViagensDetalhe";
    private Toolbar myToolbar;
    private MinhaRota minhaRota;
    private ImageView mIvMapaRota;
    private ConstraintLayout mClNovaOcorrencia;
    private TextView mTvHora, mTvData, mTvNomeMotorista, mTvMarcaVeiculo, mTvExtrasVeiculo, mTvAnoVeiculo, mTvLocalPartida, mTvLocalDestino;
    private CircleImageView mCivFotoMotorista;
    private Passageiro passageiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_viagens_detalhe);
        initViews();
        initMotorista();
        mClNovaOcorrencia.setOnClickListener(view -> showBottomSheetDialog());
    }

    private void initMotorista() {
        Date data = new Date(minhaRota.getEmbarcadoAt());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
        String dataFormatada = sdf.format(data);
        String horaFormatada = sdfHora.format(data);
        mTvHora.setText(horaFormatada);
        mTvData.setText(dataFormatada);
        mTvLocalPartida.setText(minhaRota.getPartida());
        mTvLocalDestino.setText(minhaRota.getDestino());
        mTvNomeMotorista.setText(minhaRota.getNomeMotorista());
        mTvMarcaVeiculo.setText(minhaRota.getModeloVeiculo());
        mTvExtrasVeiculo.setText(minhaRota.getPlacaVeiculo());
        mTvAnoVeiculo.setText(String.valueOf(minhaRota.getAnoVeiculo()));
        Picasso.get().load(minhaRota.getFotoPerfilMotorista()).into(mCivFotoMotorista);
        Picasso.get().load(getString(R.string.heremaps_rotas, minhaRota.getImagemMapa())).into(mIvMapaRota);
    }


    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_ocorrencia_dialog);
        LinearLayout llAcidente = bottomSheetDialog.findViewById(R.id.llEnvolviAcidente);
        LinearLayout llVeiculoQuebrou = bottomSheetDialog.findViewById(R.id.llVeiculoQuebrou);
        LinearLayout llOcorreuAssalto = bottomSheetDialog.findViewById(R.id.llOcorreuAssalto);
        LinearLayout llClienteNaoSubiu = bottomSheetDialog.findViewById(R.id.llClienteNaoSubiu);
        LinearLayout llItemsPerdidos = bottomSheetDialog.findViewById(R.id.llItemsPerdidos);
        LinearLayout llAlgoDiferente = bottomSheetDialog.findViewById(R.id.llAlgoDiferenteAconteceu);

        llAcidente.setOnClickListener(view -> {
            passageiro.setOcorrenciaEnum(ACIDENTE);
            showBottomSheetDialog2("Eu me envolvi em um acidente", passageiro);
            Log.e(TAG, "showBottomSheetDialog: " + passageiro.getOcorrenciaEnum());
        });
        llVeiculoQuebrou.setOnClickListener(view -> {
            passageiro.setOcorrenciaEnum(VEICULO_QUEBROU);
            showBottomSheetDialog2("Meu veículo quebrou", passageiro);
        });

        llOcorreuAssalto.setOnClickListener(view -> {
            passageiro.setOcorrenciaEnum(ASSALTO);
            showBottomSheetDialog2("Ocorreu um assalto", passageiro);
        });
        llClienteNaoSubiu.setOnClickListener(view -> {
            passageiro.setOcorrenciaEnum(CLIENTE_NAO_SUBIU);
            showBottomSheetDialog2("Cliente não subiu no veículo", passageiro);

        });
        llItemsPerdidos.setOnClickListener(view -> {
            passageiro.setOcorrenciaEnum(ITENS_PERDIDOS);
            showBottomSheetDialog2("Itens perdidos", passageiro);
        });
        llAlgoDiferente.setOnClickListener(view -> {
            passageiro.setOcorrenciaEnum(ALGO_DIFERENTE);
            showBottomSheetDialog2("Algo diferente aconteceu", passageiro);
        });

        bottomSheetDialog.show();
    }


    private void showBottomSheetDialog2(String causa, Passageiro passageiro) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_ocorrencia_dialog2);
        EditText mEtDataDaOcorrencia = bottomSheetDialog.findViewById(R.id.editTextDataDoAcontecimento);
        EditText mEtHoraDaOcorrencia = bottomSheetDialog.findViewById(R.id.editTextHoraDoAcontecimento);
        TextView mTvCausaOcorrencia = bottomSheetDialog.findViewById(R.id.textViewCausa);
        Button mBtnEnviarOcorrencia = bottomSheetDialog.findViewById(R.id.buttonEnviarOcorrencia);
        EditText mEtMotivoOcorrencia = bottomSheetDialog.findViewById(R.id.editTextOqueAconteceu);
        mTvCausaOcorrencia.setText(causa);
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        mEtHoraDaOcorrencia.setText(sdfHour.format(today));
        mEtDataDaOcorrencia.setText(sdfDate.format(today));

        mBtnEnviarOcorrencia.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(mEtMotivoOcorrencia.getText().toString())) {
                String motivo = mEtMotivoOcorrencia.getText().toString();
                enviarOcorrencia(passageiro, motivo);
            }else{
                mEtMotivoOcorrencia.setError("Necessário escrever um motivo.");
            }

        });
        bottomSheetDialog.show();
    }

    private void enviarOcorrencia(Passageiro passageiro, String causa) {
        try {
            Passageiro passageiro2 = new PassageiroBo(this).autenticado();

            HttpAsync httpAsync = new HttpAsync(new URL(getString(R.string.base_url) + "ocorrencia/passageiro/abrir"));
            httpAsync.addHeader("Authorization", "Bearer " + passageiro2.getToken());
            httpAsync.addParam("ocorrenciaEnum", passageiro.getOcorrenciaEnum());
            httpAsync.addParam("acontecimento", causa);
            httpAsync.addParam("dataAcontecimento", minhaRota.getEmbarcadoAt());
            httpAsync.addParam("id_passageiro", passageiro2.getId());
            httpAsync.addParam("id_viagem", minhaRota.getId());
            httpAsync.setDebug(true);
            httpAsync.post(new FutureCallback() {
                @Override
                public void onBeforeExecute() {

                }

                @Override
                public void onAfterExecute() {


                }

                @Override
                public void onSuccess(int responseCode, Object object) {

                    switch (responseCode) {
                        case 200:
                            finish();
                            startActivity(new Intent(HistoricoViagensDetalheActivity.this, HistoricoViagensDetalheActivity.class).putExtra("MINHAROTA", minhaRota));

                        case 500:

                            Util.showAlertDialog("Erro estabecelendo conexão com o servidor", HistoricoViagensDetalheActivity.this);

                            break;
                        default:

                            JSONObject jsonObject2 = (JSONObject) object;

                            try {
                                String mensagemAlert = jsonObject2.getString("mensagem");
                                Util.showAlertDialog(mensagemAlert, HistoricoViagensDetalheActivity.this);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(HistoricoViagensDetalheActivity.this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        // arrays & obj init
        passageiro = new Passageiro();

        minhaRota = (MinhaRota) getIntent().getSerializableExtra("HISTORICOVIAGEM");

        //toolbar
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.vc_back_white_arrow);
        getSupportActionBar().setTitle("Info da viagem");
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#16163F")));

        //views
        mTvHora = findViewById(R.id.textViewHora);
        mClNovaOcorrencia = findViewById(R.id.clNovaOcorrencia);
        mTvData = findViewById(R.id.textViewData);
        mIvMapaRota = findViewById(R.id.imageViewMapa);
        mTvLocalPartida = findViewById(R.id.textViewPartida);
        mTvLocalDestino = findViewById(R.id.textViewDestino);
        mTvNomeMotorista = findViewById(R.id.textViewNomeMotorista);
        mTvMarcaVeiculo = findViewById(R.id.textViewMarcaVeiculo);
        mTvExtrasVeiculo = findViewById(R.id.textViewExtrasVeiculo);
        mTvAnoVeiculo = findViewById(R.id.textViewAnoVeiculo);
        mCivFotoMotorista = findViewById(R.id.imageViewMotorista);
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