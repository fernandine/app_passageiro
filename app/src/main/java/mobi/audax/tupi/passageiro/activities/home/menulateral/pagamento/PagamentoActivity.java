package mobi.audax.tupi.passageiro.activities.home.menulateral.pagamento;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.activities.login.LoginActivity;
import mobi.audax.tupi.passageiro.adapter.PagamentoAdapter;
import mobi.audax.tupi.passageiro.bin.bo.PassageiroBo;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.controller.PagamentoController;
import mobi.audax.tupi.passageiro.bin.dto.ListaCartaoDto;
import mobi.audax.tupi.passageiro.bin.dto.Pagamento;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class PagamentoActivity extends AppCompatActivity {

    int LAUNCH_ADICIONAR_CARTAO_ACTIVITY = 1;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setListCartoesCadastrado();
    }

    private void setListCartoesCadastrado() {
        var passageiro = new PassageiroBo(this).autenticado();
        var passager = new PassengerBo(this).autenticado();
        PassengerBo passengerBo = new PassengerBo(this);

        if (passageiro != null && passageiro.getHash() != null) {
            var controller = new PagamentoController(this);
            controller.getListCartao(() -> {
                progressDialog = ProgressDialog.show(this, null, "Buscando", true);
                return null;
            }, (statusCode, cartoes) -> {
                dismissProgressDialog();
                if (statusCode == 200) {
                    setCartoes(cartoes);
                } else {
                    showMessageSemCartao();
                }
                return null;
            });
        } else {
            showMessageNaoAutenticado();
        }
    }

    private void setCartoes(List<ListaCartaoDto> cartoes) {
        if (!cartoes.isEmpty()) {
            var pagamentos = new ArrayList<Pagamento>();
            List<Pagamento> pagamentosCartoes = getCartoesForPagamento(cartoes, pagamentos);
            if (!pagamentosCartoes.isEmpty()) {
                var adapter = new PagamentoAdapter(this, pagamentos);
                var listView = (ListView) findViewById(R.id.list);
                listView.setAdapter(adapter);
                listView.setOnItemLongClickListener((parent, view, position, id) -> {
                    Pagamento pagamentoSelecionado = (Pagamento) parent.getItemAtPosition(position);
                    showMessageExcluirCartao(pagamentoSelecionado);
                    return false;
                });
            } else {
                showMessageSemCartao();
            }
        } else {
            showMessageSemCartao();
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
            Intent intent = new Intent(this, AdicionarNovoCartaoActivity.class);
            startActivityForResult(intent, LAUNCH_ADICIONAR_CARTAO_ACTIVITY);
        }
    }

    private void showMessageSemCartao() {
        Util.alert(this, R.string.cartoes_cadastrado, R.string.cartoes_nao_encontrados, R.string.cadastrar_cartao, () -> {
            Intent intent = new Intent(this, AdicionarNovoCartaoActivity.class);
            startActivityForResult(intent, LAUNCH_ADICIONAR_CARTAO_ACTIVITY);
        }, R.string.ok);
    }

    private void showMessageExcluirCartao(Pagamento pagamentoSelecionado) {
        Util.alert(this, R.string.atencao, R.string.excluir_cartao_cadastrado, R.string.excluir, () -> {
            var controller = new PagamentoController(this);
            controller.deleteCartao(pagamentoSelecionado.getId(), () -> {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.excluindo), true);
                return null;
            }, statusCode -> {
                dismissProgressDialog();
                if (statusCode == 200) {
                    Util.showAlertDialog(getString(R.string.sucesso), getString(R.string.registro_salvo_sucesso), this);
                } else {
                    Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this);
                }
                return null;
            });
            setListCartoesCadastrado();
        }, R.string.fechar);
    }

    private void showMessageNaoAutenticado() {
        Util.alert(this, R.string.usuario_nao_autenticado, R.string.necessario_autenticacao, R.string.entrar_ou_criar_conta, () -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        }, R.string.ok);
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_ADICIONAR_CARTAO_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                setListCartoesCadastrado();
            }
        }
    }

}