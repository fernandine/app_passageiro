package mobi.audax.tupi.passageiro.activities.cadastro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.vinaygaba.creditcardview.CreditCardView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.activities.splash.SplashActivity;
import mobi.audax.tupi.passageiro.activities.home.HomeActivity;
import mobi.audax.tupi.passageiro.bin.bean.Cartao;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bean.Pessoa;
import mobi.audax.tupi.passageiro.bin.controller.PassageiroController;
import mobi.audax.tupi.passageiro.bin.enumm.CardType;
import mobi.audax.tupi.passageiro.bin.dto.GoogleAuth;
import mobi.audax.tupi.passageiro.bin.dto.Login;
import mobi.audax.tupi.passageiro.bin.util.MaskTelefone;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class AdicionarCartaoActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private Passenger passenger;
    private boolean google = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_cartao);

        initViews();
    }

    private void initViews() {
        passenger = (Passenger) getIntent().getSerializableExtra(Passenger.class.getSimpleName());
        google = getIntent().getBooleanExtra("google", false);

        //views
        var holder = (TextInputLayout) findViewById(R.id.holder);
        var numeroCartao = (TextInputLayout) findViewById(R.id.numeroCartao);
        var validade = (TextInputLayout) findViewById(R.id.dataValidade);

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
                case MASTERCARD -> creditCard.setType(com.vinaygaba.creditcardview.CardType.MASTERCARD);
                case AMERICAN_EXPRESS -> creditCard.setType(com.vinaygaba.creditcardview.CardType.AMERICAN_EXPRESS);
                case DISCOVER -> creditCard.setType(com.vinaygaba.creditcardview.CardType.DISCOVER);
                default -> creditCard.setType(com.vinaygaba.creditcardview.CardType.AUTO);
            }

        }));
        validade.getEditText().addTextChangedListener(cardFill(creditCard::setExpiryDate));
        findViewById(R.id.btnAdicionarCartao).setOnClickListener(view -> salvar());
    }

    private interface OnAfterTextCardChanged {
        void onChange(String text);
    }

    @NonNull
    @Contract("_ -> new")
    private TextWatcher cardFill(final OnAfterTextCardChanged callback) {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { callback.onChange(s.toString()); }
        };
    }

    private void salvar() {
        if (formOk()) {
            var numero = (TextInputLayout) findViewById(R.id.numeroCartao);
            var cvv = (TextInputLayout) findViewById(R.id.cvv);
            var validade = (TextInputLayout) findViewById(R.id.dataValidade);
            var cep = (TextInputLayout) findViewById(R.id.cep);
            var nome = (TextInputLayout) findViewById(R.id.holder);

//            if (passenger.getCartao() == null) {
//                passenger.setCartao(new Cartao());
//            }
//            if (passenger.getPessoa() == null) {
//                passenger.setPessoa(new Pessoa());
//            }

            passenger.getCartao().setHolder(nome.getEditText().getText().toString());
            passenger.getCartao().setNumero(Util.onlyNumber( numero.getEditText().getText().toString() ));
            passenger.getCartao().setCvv(cvv.getEditText().getText().toString());
            var dataValidade = validade.getEditText().getText().toString();
            passenger.getCartao().setMesExpiracao( dataValidade.substring(0, 2) );
            passenger.getCartao().setAnoExpiracao(dataValidade.substring(3, 5));
            passenger.getPessoa().setCep(Integer.parseInt(Util.onlyNumber(cep.getEditText().getText().toString())));

            var caminho = passenger.getPessoa().getFotoPerfil().getCaminho();
            if (caminho != null && !caminho.startsWith("http")) {
                var path = passenger.getPessoa().getFotoPerfil().getCaminho();
                var base64 = Util.imageToBase64(path);
                passenger.getPessoa().getFotoPerfil().setCaminho(base64);
            }

            var controller = new PassageiroController(this);
            controller.create(passenger, () -> {
                dismissProgressDialog();
                progressDialog = ProgressDialog.show(this, null, getString(R.string.realizando_cadastro), true);
                return null;
            }, statusCode -> {
                dismissProgressDialog();

                switch (statusCode) {
                    case 201:
                        if (google) {
                            loginGoogle();
                        } else {
                            login();
                        }
                        break;
                    case 409:
                        Util.showAlertDialog(getString(R.string.atencao), getString(R.string.passageiro_informado_ja_cadastrado_sistema), this, () -> {
                            finishAffinity();
                            startActivities(new Intent[] {
                                    new Intent(this, SplashActivity.class),
                                    new Intent(this, LoginActivity.class),
                            });
                        });
                        break;
                    case 417:
                        Util.showAlertDialog(getString(R.string.preencha_todos_dados_processeguir_cadastro), this);
                        break;
                    default:
                        Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
                        break;
                }

                return null;
            });

        }
    }

    private boolean formOk() {
        var ok = true;

        var inputs = new TextInputLayout[]{
                findViewById(R.id.numeroCartao),
                findViewById(R.id.dataValidade),
                findViewById(R.id.cvv),
                findViewById(R.id.holder),
                findViewById(R.id.cep)
        };

        for (var input : inputs) {
            input.setErrorEnabled(false);
            if (StringUtils.isBlank(input.getEditText().getText().toString())) {
                input.setErrorEnabled(true);
                input.setError(getString(R.string.campo_obrigatorio));
                ok = false;
            }
        }
        return ok;
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void login() {
        var email = passenger.getEmail();
        var senha = passenger.getSenha();
        var token = new Prefs(this).getToken();

        var controller = new PassageiroController(this);
        controller.login(new Login(email, senha, token), () -> {
            dismissProgressDialog();
            progressDialog = ProgressDialog.show(this, null, getString(R.string.acessando), true);
            return null;
        }, statusCode -> {
            dismissProgressDialog();
            switch (statusCode) {
                case 200 -> {
                    finishAffinity();
                    startActivities(new Intent[] {
                            new Intent(this, SplashActivity.class),
                            new Intent(this, HomeActivity.class)
                    });
                }
                case 401, 412 -> Util.showAlertDialog(getString(R.string.passageiro_nao_encontrado), this);
                case 999 -> Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    private void loginGoogle() {
        var firebase = passenger.getFireBaseId();
        var token = new Prefs(this).getToken();

        var controller = new PassageiroController(this);
        controller.googleLogin(new GoogleAuth(firebase, token), () -> {
            dismissProgressDialog();
            progressDialog = ProgressDialog.show(this, null, getString(R.string.acessando), true);
            return null;
        }, statusCode -> {
            dismissProgressDialog();
            switch (statusCode) {
                case 200 -> {
                    finishAffinity();
                    startActivities(new Intent[] {
                            new Intent(this, SplashActivity.class),
                            new Intent(this, HomeActivity.class)
                    });
                }
                case 401, 412 -> Util.showAlertDialog(getString(R.string.passageiro_nao_encontrado), this);
                case 999 -> Util.showAlertDialog(getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}