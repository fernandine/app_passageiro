package mobi.audax.tupi.passageiro.activities.recuperar_senha

import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_recuperar_senha.*
import mobi.audax.tupi.passageiro.R
import mobi.audax.tupi.passageiro.bin.controller.PassageiroController
import mobi.audax.tupi.passageiro.bin.util.Util
import org.apache.commons.lang3.StringUtils

class RecuperarSenhaActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        toolbar.setTitle(R.string.recuperar_senha)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onRecuperar(view: View) {
        email.isErrorEnabled = false
        val correio = email.editText?.text.toString()
        if (StringUtils.isBlank(correio)) {
            email.error = getText(R.string.campo_obrigatorio)
            email.isErrorEnabled = true
        } else if (!Util.validateEmail(correio)) {
            email.error = getText(R.string.email_invalido)
            email.isErrorEnabled = true
        } else {
            val controller = PassageiroController(this)
            controller.recuperarSenha(correio, {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.solicitando_recuperacao_senha), true)
            }) { statusCode ->
                progressDialog?.let { progress ->
                    if (progress.isShowing)
                        progress.dismiss()
                }

                when (statusCode) {
                    200 -> {
                        Util.showAlertDialog(getString(R.string.sucesso), getString(R.string.solicitacao_recuperacao_senha_sucesso), this) {
                            finish()
                        }
                    }
                    412 -> {
                        Util.showAlertDialog(getString(R.string.erro), getString(R.string.email_informado_nao_existe_recuperar_senha), this)
                    }
                    else -> {
                        Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this)
                    }
                }
            }
        }
    }

}