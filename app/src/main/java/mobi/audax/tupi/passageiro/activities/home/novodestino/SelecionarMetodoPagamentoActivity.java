package mobi.audax.tupi.passageiro.activities.home.novodestino;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.home.menulateral.pagamento.AdicionarNovoCartaoActivity;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.adapter.PagamentoAdapter;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.controller.PagamentoController;
import mobi.audax.tupi.passageiro.bin.dto.ListaCartaoDto;
import mobi.audax.tupi.passageiro.bin.dto.Pagamento;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class SelecionarMetodoPagamentoActivity extends AppCompatActivity {

    int LAUNCH_ADICIONAR_CARTAO_ACTIVITY = 1;
    int LAUNCH_PAGAMENTO_PIX_ACTIVITY = 2;

    private TextView mTvSemCartoes;
    private ProgressDialog progressDialog;
    private Passenger passenger;
    private String valor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_metodo_pagamento);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        passenger = new PassengerBo(this).autenticado();
        valor = getIntent().getStringExtra("valor");

        initView();
    }

    private void initView() {
        mTvSemCartoes = findViewById(R.id.textViewListaVazia);
        View viewAdicionarCartao = findViewById(R.id.layoutAdicionarCartao);
        LinearLayout viewPix = findViewById(R.id.layoutPix);
        ImageView iconPix = findViewById(R.id.iconPix);
        iconPix.setImageResource(R.drawable.ic_pix);

        viewAdicionarCartao.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdicionarNovoCartaoActivity.class);
            startActivityForResult(intent, LAUNCH_ADICIONAR_CARTAO_ACTIVITY);
        });

        viewPix.setOnClickListener(view -> {
            Intent intent = new Intent(this, PagamentoPixActivity.class);
            intent.putExtra("valor", valor);
            startActivityForResult(intent, LAUNCH_PAGAMENTO_PIX_ACTIVITY);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (passenger != null && passenger.getHash() != null) {
            var controller = new PagamentoController(this);
            controller.getListCartao(() -> {
                progressDialog = ProgressDialog.show(this, null, "Buscando", true);
                return null;
            }, (statusCode, cartoes) -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (statusCode == 200) {
                    setCartoes(cartoes);
                    mTvSemCartoes.setVisibility(View.GONE);
                } else {
                    mTvSemCartoes.setVisibility(View.VISIBLE);
                }
                return null;
            });
        } else {
            showMessageNaoAutenticado();
        }
    }

    private void setCartoes(List<ListaCartaoDto> cartoes) {
        var pagamentos = new ArrayList<Pagamento>();
        List<Pagamento> pagamentosCartoes = getCartoesForPagamento(cartoes, pagamentos);
        if (!pagamentosCartoes.isEmpty()) {
            var adapter = new PagamentoAdapter(this, pagamentos);
            var listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Pagamento pagamentoSelecionado = (Pagamento) parent.getItemAtPosition(position);
                String pagamentoJson = new Gson().toJson(pagamentoSelecionado);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("pagamentoSelecionado", pagamentoJson);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            });
        }
    }

    private List<Pagamento> getCartoesForPagamento(List<ListaCartaoDto> listCartoes, ArrayList<Pagamento> pagamentos) {
        return listCartoes.stream()
                .filter(this::cartaoIsValid)
                .flatMap(cartao -> {
                    pagamentos.add(new Pagamento(cartao));
                    return pagamentos.stream();
                }).collect(Collectors.toList());
    }

    private boolean cartaoIsValid(ListaCartaoDto cartao) {
        if (cartao.getId() <= 0)
            return false;
        return cartao.getCreditCardNumber() != null;
    }

    public void onAddNovaFormaPagamento(View view) {
        var passenger = new PassengerBo(this).autenticado();
        if (passenger == null) {
            showMessageNaoAutenticado();
        } else {
            startActivity(new Intent(this, AdicionarNovoCartaoActivity.class));
        }
    }

    private void showMessageNaoAutenticado() {
        Util.alert(this, R.string.usuario_nao_autenticado, R.string.necessario_autenticacao, R.string.entrar_ou_criar_conta, () -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        }, R.string.ok);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
