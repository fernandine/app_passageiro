package mobi.audax.tupi.passageiro.activities.home.menulateral.dados;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo;
import mobi.audax.tupi.passageiro.bin.controller.PassageiroController;
import mobi.audax.tupi.passageiro.bin.dto.PassageiroUpdateRequest;
import mobi.audax.tupi.passageiro.bin.util.MaskTelefone;
import mobi.audax.tupi.passageiro.bin.util.Util;
import mobi.audax.tupi.passageiro.bin.util.WriteSDCard;

public class EditarDadosActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private final int REQUEST_TAKE_PHOTO = 1;
    private File photoFile;
    private String b64;
    private Passenger passenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dados);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    private void initViews() {
        this.passenger = new PassengerBo(this).autenticado();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            findViewById(R.id.senhaContainer).setVisibility(View.GONE);
        }
        var telefone = (EditText) findViewById(R.id.telefone);
        telefone.addTextChangedListener(MaskTelefone.insert("(##)#####-####", telefone));

        var cep = (EditText) findViewById(R.id.cep);
        cep.addTextChangedListener(MaskTelefone.insert("#####-###", cep));

        if (passenger != null && passenger.getPessoa() != null) {
            ((TextView) findViewById(R.id.nome)).setText(passenger.getPessoa().getNome());
            ((TextView) findViewById(R.id.email)).setText(passenger.getEmail());
            ((TextView) findViewById(R.id.telefone)).setText(passenger.getPessoa().getCelular());
            ((TextView) findViewById(R.id.cidade)).setText(passenger.getCidade());
            ((TextView) findViewById(R.id.cep)).setText(String.valueOf(passenger.getPessoa().getCep()));
            if (passenger.getPessoa().getFotoPerfil() != null) {
                Picasso.get().load(passenger.getPessoa().getFotoPerfil().getCaminho()).placeholder(R.drawable.vc_logo_tupi).into((CircleImageView) findViewById(R.id.foto));
            }
        }
    }

    @NonNull
    private String extractText(int res) {
        return ((EditText) findViewById(res)).getText().toString();
    }

    public void onSalvar(View view) {
        if (formOk()) {
            this.sendToServer();
        }
    }

    private void sendToServer() {
        var pur = new PassageiroUpdateRequest();
        pur.setId(passenger.getId());
        pur.setNome(extractText(R.id.nome));
        pur.setEmail(extractText(R.id.email));
        pur.setCelular(extractText(R.id.telefone));
        pur.setCidade(extractText(R.id.cidade));
        pur.setCep(Integer.parseInt(Util.onlyNumber(extractText(R.id.cep))));
        pur.setSenha(extractText(R.id.novaSenha));
        if (StringUtils.isNotBlank(b64)) {
            pur.setFoto(b64);
        }

        Log.v("sendtoserver", pur.getNome() + " ==> " + pur.getEmail());

        var controller = new PassageiroController(this);
        controller.update(pur, () -> {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.salvando), true);
            return null;
        }, statusCode -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (statusCode == 200) {
                Util.showAlertDialog(getString(R.string.sucesso), getString(R.string.cadastro_atualizado_sucesso), this, this::finish);
            } else if (statusCode == 412) {
                Util.showAlertDialog(getString(R.string.erro), getString(R.string.registro_nao_encontrado_feche_tente_novamente), this);
            } else {
                Util.showAlertDialog(getString(R.string.erro), getString(R.string.erro_estabelecer_conexao_servidor), this);
            }
            return null;
        });
    }

    private boolean formOk() {
        boolean ok = true;
        var arrs = new EditText[]{
                findViewById(R.id.nome),
                findViewById(R.id.telefone),
                findViewById(R.id.cidade),
                findViewById(R.id.cep)
        };
        for (var editText : arrs) {
            if (StringUtils.isBlank(editText.getText().toString())) {
                editText.setError(getString(R.string.campo_obrigatorio));
                ok = false;
            }
        }
        var email = (EditText) findViewById(R.id.email);
        if (StringUtils.isBlank(email.getText().toString()) || !Util.validateEmail(email.getText().toString())) {
            email.setError(getString(R.string.email_invalido));
            ok = false;
        }

        if (findViewById(R.id.senhaContainer).getVisibility() == View.VISIBLE) {
            if (StringUtils.isNotBlank(extractText(R.id.novaSenha))) {
                var senhaAtual = extractText(R.id.senhaAtual);
                if (BCrypt.checkpw(senhaAtual, passenger.getSenha())) {
                    if (!StringUtils.equals(extractText(R.id.novaSenha), extractText(R.id.novaSenhaConfirmar))) {
                        var novaSenhaConfirmar = (EditText) findViewById(R.id.novaSenhaConfirmar);
                        novaSenhaConfirmar.setError(getString(R.string.senha_diferentes));
                        ok = false;
                    }
                } else {
                    ((EditText) findViewById(R.id.senhaAtual)).setError(getString(R.string.senha_nao_confere));
                    ok = false;
                }
            }
        }

        return ok;
    }

    @Nullable
    private File getOutputMediaFile() {
        try {
            return new WriteSDCard(this).createFilePath("IMG_" + Calendar.getInstance().getTimeInMillis() + "_" + REQUEST_TAKE_PHOTO + ".jpg");
        } catch (Exception e) {
            return null;
        }
    }

    public void onTirarFoto(View view) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                photoFile = getOutputMediaFile();
                if (photoFile != null) {
                    var uri = FileProvider.getUriForFile(this, "mobi.audax.tupi.passageiro.provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (photoFile != null && photoFile.exists()) {
                    Glide.with(this).load(photoFile).into((CircleImageView) findViewById(R.id.foto));
                    new Thread(() -> b64 = Util.fileToBase64(photoFile)).start();
                } else {
                    Toast.makeText(this, R.string.erro_capturar_foto, Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (photoFile != null && photoFile.exists()) {
                    photoFile.delete();
                    photoFile = null;
                }
                Util.alert(this, R.string.atencao, R.string.mensagem_erro_foto_cancelada, R.string.ok);
            }
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