package mobi.audax.tupi.passageiro.activities.home.menulateral.pagamento;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.vinaygaba.creditcardview.CreditCardView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.controller.PagamentoController;
import mobi.audax.tupi.passageiro.bin.dto.CartaoDto;
import mobi.audax.tupi.passageiro.bin.enumm.CardType;
import mobi.audax.tupi.passageiro.bin.util.MaskTelefone;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class AdicionarNovoCartaoActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_novo_cartao);
        initViews();
    }

    public void onSalvarCartao() {
        if (formOk()) {

            var numero = (TextInputLayout) findViewById(R.id.numeroCartao);
            var validade = (TextInputLayout) findViewById(R.id.dataValidade);
            var cvv = (TextInputLayout) findViewById(R.id.cvv);
            var holder = (TextInputLayout) findViewById(R.id.holder);

            String numeroText = Util.onlyNumber(numero.getEditText().getText().toString());
            String cvvText = cvv.getEditText().getText().toString();
            String holderText = holder.getEditText().getText().toString();

            var dataValidade = validade.getEditText().getText().toString();
            String mesText = dataValidade.substring(0, 2);
            String anoText = dataValidade.substring(3, 5);


            int tipo = 0;
            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            if (selectedRadioButtonId != -1) {
                RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                String selectedValue = selectedRadioButton.getText().toString();
                if (selectedValue.equals(R.string.debito))
                    tipo = 1;
            }

            var cartao = new CartaoDto();
            cartao.setTipoCartao(tipo);
            cartao.setAnoExpiracao(anoText);
            cartao.setMesExpiracao(mesText);
            //cartao.setNumero(numeroText);
            cartao.setDefaultPayment(false);
            cartao.setCvv(cvvText);
            cartao.setCreditCardBrand("visa");
            cartao.setCreditCardNumber(numeroText);
            cartao.setHolder(holderText);

            var passenger = new PassengerBo(this).autenticado();
            if (passenger != null) {
                var controller = new PagamentoController(this);
                controller.createCartao(cartao, () -> {
                    progressDialog = ProgressDialog.show(this, null, getString(R.string.salvando), true);
                    return null;
                }, statusCode -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    switch (statusCode) {
                        case 200 -> {
                            Util.showAlertDialog(getString(R.string.sucesso), getString(R.string.registro_salvo_sucesso), this);
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                        case 403 ->
                                Util.loginExpirado(this);
                        case 404 ->
                                Util.showAlertDialog(getString(R.string.erro), getString(R.string.cartao_nao_encontrado), this);
                        case 409 ->
                                Util.showAlertDialog(getString(R.string.erro), getString(R.string.atualizacao_cartao_nao_autorizado), this);
                        default ->
                                Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this);
                    }
                    return null;
                });
            } else {
                showMessageNaoAutenticado();
            }
        }
    }

    private void showMessageNaoAutenticado() {
        Util.alert(this, R.string.usuario_nao_autenticado, R.string.cadastrar_cartao_nao_autenticado, R.string.entrar_ou_criar_conta, () -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        }, R.string.ok);
    }

    public boolean formOk() {
        var ok = true;
        var inputs = new TextInputLayout[]{
                findViewById(R.id.numeroCartao),
                findViewById(R.id.dataValidade),
                findViewById(R.id.cvv),
                findViewById(R.id.holder)
        };

        for (var input : inputs) {
            input.setErrorEnabled(false);
            if (StringUtils.isBlank(Objects.requireNonNull(input.getEditText()).getText().toString())) {
                input.setErrorEnabled(true);
                input.setError(getString(R.string.campo_obrigatorio));
                ok = false;
            }
        }

        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(getApplicationContext(), "Por favor, selecione a função do cartão", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    private interface OnAfterTextCardChanged {
        void onChange(String text);
    }

    @NonNull
    @Contract("_ -> new")
    private TextWatcher cardFill(final OnAfterTextCardChanged callback) {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                callback.onChange(s.toString());
            }
        };
    }

    private void initViews() {
        findViewById(R.id.btnAdicionarCartao).setOnClickListener(view -> onSalvarCartao());

        var holder = (TextInputLayout) findViewById(R.id.holder);
        var numeroCartao = (TextInputLayout) findViewById(R.id.numeroCartao);
        var validade = (TextInputLayout) findViewById(R.id.dataValidade);
        radioGroup = findViewById(R.id.radioGroup);

        Objects.requireNonNull(numeroCartao.getEditText()).addTextChangedListener(MaskTelefone.insert("####-####-####-####", numeroCartao.getEditText()));
        Objects.requireNonNull(validade.getEditText()).addTextChangedListener(MaskTelefone.insert("##/##", validade.getEditText()));

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        CreditCardView creditCard = findViewById(R.id.card);
        creditCard.setBackgroundResource(R.drawable.ic_background_card);
        creditCard.setType(com.vinaygaba.creditcardview.CardType.AUTO);

        Objects.requireNonNull(holder.getEditText()).addTextChangedListener(cardFill(creditCard::setCardName));
        numeroCartao.getEditText().addTextChangedListener(cardFill(text -> {
            text = Util.onlyNumber(text);
            creditCard.setCardNumber(text);

            var type = CardType.detect(text);
            Log.v("CARD", "Type: " + type);
            switch (type) {
                case VISA -> creditCard.setType(com.vinaygaba.creditcardview.CardType.VISA);
                case MASTERCARD ->
                        creditCard.setType(com.vinaygaba.creditcardview.CardType.MASTERCARD);
                case AMERICAN_EXPRESS ->
                        creditCard.setType(com.vinaygaba.creditcardview.CardType.AMERICAN_EXPRESS);
                case DISCOVER -> creditCard.setType(com.vinaygaba.creditcardview.CardType.DISCOVER);
                default -> creditCard.setType(com.vinaygaba.creditcardview.CardType.AUTO);
            }
        }));
        validade.getEditText().addTextChangedListener(cardFill(creditCard::setExpiryDate));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}