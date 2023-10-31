package mobi.audax.tupi.passageiro.activities.home.novodestino;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.controller.PagamentoController;
import mobi.audax.tupi.passageiro.bin.dto.Pix;
import mobi.audax.tupi.passageiro.bin.dto.PixDto;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class PagamentoPixActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private double valor = 50.0;
    private ProgressBar progressBar;
    private EditText textCodigoPix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento_pix);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textCodigoPix = findViewById(R.id.chavepix);
        getPixValues();
        valor = getIntent().getDoubleExtra("valor", 0.0);
    }

    private void initView(PixDto pixDto) {

        textCodigoPix.setText(pixDto.getText());

        Button buttonCopiarPix = findViewById(R.id.buttonCopiarPix);

        buttonCopiarPix.setOnClickListener(view -> {

            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Chave", textCodigoPix.getText().toString());
            clipboardManager.setPrimaryClip(clipData);

            var dialog = new AlertDialog.Builder(this);
            final var customLayout = getLayoutInflater().inflate(R.layout.dialog_alert_image, null);
            var mBtnAlert = (MaterialButton) customLayout.findViewById(R.id.dialogButtonConfirm);
            dialog.setView(customLayout);
            final var alertDialog = dialog.create();
            mBtnAlert.setOnClickListener(v -> alertDialog.dismiss());

            alertDialog.create();
            alertDialog.show();
        });

        String imageUrl = pixDto.getLinks().get(0).getHref();
        ImageView viewById = findViewById(R.id.imageQrCode);
        Glide.with(this)
                .load(imageUrl)
                .into(viewById);

        progressBar = findViewById(R.id.progressBarPix);

    }

    private void getPixValues() {
        var passenger = new PassengerBo(this).autenticado();
        if (passenger != null && passenger.getHash() != null && passenger.getPushId() != null) {

            var pagamentoController = new PagamentoController(this);

            Pix pix = new Pix();
            pix.setHash(passenger.getHash());
            pix.setPushToken(passenger.getPushId());
            pix.setTaxId("0923019839081");

            pagamentoController.createPix(pix, () -> {
                progressDialog = ProgressDialog.show(this, null, "Buscando", true);
                return null;
            }, (statusCode, pixDto) -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (statusCode == 200) {
                    String id = pixDto.getId();
                    String expirationDate = null;

                    if (pixDto != null) {
                        runOnUiThread(() -> initView(pixDto));
                    }

                    if (expirationDate != null) {
                        Calendar calendar = Calendar.getInstance();
                        Date currentDate = calendar.getTime();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pt"));
                        Date backendDate = null;
                        try {
                            backendDate = dateFormat.parse(expirationDate);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        long diffMilliseconds = backendDate.getTime() - currentDate.getTime();

                        CountDownTimer countDownTimer = new CountDownTimer(diffMilliseconds, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                long secondsLeft = millisUntilFinished / 1000;
                                progressBar.setProgress((int) secondsLeft);
                            }

                            @Override
                            public void onFinish() {
                                Util.alert(PagamentoPixActivity.super.getParent(), R.string.atencao, R.string.pix_expirado);
                            }
                        };

                        countDownTimer.start();
                    }
                }
                return null;
            });
        }
    }

    private void showMessageSemValor() {
        Util.alert(this, R.string.viagem_sem_valor, R.string.verificar_valor_viagem, R.string.ok);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
