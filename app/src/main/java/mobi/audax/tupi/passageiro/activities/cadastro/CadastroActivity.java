package mobi.audax.tupi.passageiro.activities.cadastro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;

import org.apache.commons.lang3.StringUtils;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bean.Pessoa;
import mobi.audax.tupi.passageiro.bin.util.CpfCnpjMaks;
import mobi.audax.tupi.passageiro.bin.util.DocsIntent;
import mobi.audax.tupi.passageiro.bin.util.MaskTelefone;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.Util;
import mobi.audax.tupi.passageiro.databinding.ActivityCadastroBinding;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;

    private Passenger passenger;
    private boolean google;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        google = getIntent().getBooleanExtra("google", false);
        if (getIntent().hasExtra(Passenger.class.getSimpleName())) {
            passenger = (Passenger) getIntent().getSerializableExtra(Passenger.class.getSimpleName());
        } else {
            passenger = new Passenger();
        }
        initViews();
        preDataBinding();
    }

    private void preDataBinding() {
        if (passenger != null) {
            binding.editTextCidade.setText(passenger.getCidade());
            binding.editTextComplemento.setText(passenger.getComplemento());
            binding.editTextTelefone.setText(passenger.getPessoa().getCelular());
            binding.editTextCpf.setText(passenger.getPessoa().getCpf());
            binding.editTextEmail.setText(passenger.getEmail());
            var fullname = passenger.getPessoa().getNome();
            if (StringUtils.isNotBlank(fullname)) {
                var nomes = fullname.split(" ");
                if (nomes.length > 0) {
                    binding.editTextNome.setText(nomes[0]);
                }
                if (nomes.length > 1) {
                    binding.editTextSobreNome.setText(nomes[1]);
                }
            }
        }
    }

    private void save() {


        if (formOk()) {
            if (passenger.getPessoa() == null) {
                passenger.setPessoa(new Pessoa());
            }
            passenger.getPessoa().setCpf(Util.onlyNumber(binding.editTextCpf.getText().toString()));
            passenger.getPessoa().setNome(binding.editTextNome.getText().toString() + " " + binding.editTextSobreNome.getText().toString());
            passenger.getPessoa().setCelular(binding.editTextTelefone.getText().toString());

            passenger.setEmail(binding.editTextEmail.getText().toString());
            passenger.setCidade(binding.editTextCidade.getText().toString());
            passenger.setComplemento(binding.editTextComplemento.getText().toString());

            passenger.setSenha(binding.txtInputSenha.getText().toString());
            passenger.setPushId(new Prefs(this).getToken());

            var intent = new Intent(this, TirarFotoActivity.class);
            intent.putExtra(Passenger.class.getSimpleName(), passenger);
            intent.putExtra("google", google);
            startActivity(intent);
        }
    }

    private boolean formOk() {
        var ok = true;

        var ids = new int[]{
                R.id.editTextNome,
                R.id.editTextSobreNome,
                R.id.editTextCpf,
                R.id.editTextEmail,
                R.id.editTextTelefone,
                R.id.editTextCidade

        };

        for (var id : ids) {
            var et = (EditText) findViewById(id);
            if (StringUtils.isEmpty(et.getText().toString())) {
                et.setError(getString(R.string.campo_obrigatorio));
                ok = false;
            }
        }
        if (ok) {
            var cpf = (EditText) findViewById(R.id.editTextCpf);
            var email = (EditText) findViewById(R.id.editTextEmail);

            if (!Util.isCPF(cpf.getText().toString())) {
                cpf.setError(getString(R.string.cpf_invalido));
                ok = false;
            }
            if (!Util.validateEmail(email.getText().toString())) {
                email.setError(getString(R.string.email_invalido));
                ok = false;
            }
        }
        if (!google) {
            var senha = (TextInputEditText) findViewById(R.id.txtInputSenha);
            if (StringUtils.isEmpty(senha.getText().toString())) {
                senha.setError(getString(R.string.campo_obrigatorio));
                ok = false;
            }
        }
        return ok;
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.editTextTelefone.addTextChangedListener(MaskTelefone.insert("(##)#####-####", binding.editTextTelefone));
        binding.editTextCpf.addTextChangedListener(CpfCnpjMaks.insert(binding.editTextCpf));

        findViewById(R.id.btnContinuarCadastro).setOnClickListener(view -> save());

        // region link builder.
        var link = new Link("Termos de Uso")
                .setTextColor(Color.parseColor("#329892"))
                .setTextColorOfHighlightedLink(Color.parseColor("#0D3D0C"))
                .setHighlightAlpha(.4f)
                .setUnderlined(false)
                .setBold(true)
                .setOnClickListener((Link.OnClickListener) clickedText -> DocsIntent.termos(this));

        var link2 = new Link("Política de Privacidade")
                .setTextColor(Color.parseColor("#329892"))
                .setTextColorOfHighlightedLink(Color.parseColor("#0D3D0C"))
                .setHighlightAlpha(.4f)
                .setUnderlined(false)
                .setBold(true)
                .setOnClickListener((Link.OnClickListener) clickedText -> DocsIntent.privacidade(this));

        LinkBuilder.on(binding.politicaPrivacidade)
                .addLink(link)
                .addLink(link2)
                .build();
        // endregion

        if (google) {
            binding.editTxtPassword.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}