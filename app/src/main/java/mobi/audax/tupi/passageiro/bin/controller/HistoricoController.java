package mobi.audax.tupi.passageiro.bin.controller;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.URL;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.Passageiro;
import mobi.audax.tupi.passageiro.bin.bean.Passenger;
import mobi.audax.tupi.passageiro.bin.callback.BeforeCallback;
import mobi.audax.tupi.passageiro.bin.callback.CompleteCallback;
import mobi.stos.httplib.HttpAsync;
import mobi.stos.httplib.inter.FutureCallback;

public class HistoricoController {

    private Context context;


    public HistoricoController(Context context){
        this.context = context;
    }


    public void getListHistorico(Passenger passageiro, BeforeCallback beforeCallback, CompleteCallback completeCallback) {

        try {
            HttpAsync httpAsync = new HttpAsync(new URL(context.getString(R.string.base_url) + "rota/passageiro/historicoViagens?limite=10&pagina=1&id_passageiro=" + passageiro.getId()));
            httpAsync.addHeader("Authorization", "Bearer " + passageiro.getToken());
            httpAsync.setDebug(true);
            httpAsync.get(new FutureCallback() {
                @Override
                public void onBeforeExecute() {
                    Log.e("Historico", "onBeforeExecute: onBeforeExecute "  );
                    beforeCallback.onBefore();
                }

                @Override
                public void onAfterExecute() {
                    // não usar...
                }

                @Override
                public void onSuccess(int responseCode, Object object) {
                    Log.e("Historico", "onSuccess: responseCode " + responseCode );
                    if(object != null){
                        Log.e("Historico", "onSuccess: object " + object );
                    }
                    completeCallback.onComplete(responseCode, object);
                }

                @Override
                public void onFailure(Exception exception) {
                    completeCallback.onComplete(999, null);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
