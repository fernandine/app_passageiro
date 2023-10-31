package mobi.audax.tupi.passageiro.services;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.audax.tupi.passageiro.activities.splash.SplashActivity;
import mobi.audax.tupi.passageiro.bin.bean.Passageiro;
import mobi.audax.tupi.passageiro.bin.util.Notification;
import mobi.audax.tupi.passageiro.bin.util.Prefs;
import mobi.audax.tupi.passageiro.bin.util.SNotification;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i("PUSH TOKEN", "TOKEN: " + token);

        var prefs = new Prefs(this);
        prefs.setToken(token);
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("FCM", "onMessageReceived");
        Log.e("FCM", "MyFirebaseMessagingService >>>>>>>> " +  remoteMessage.getData());

        final Intent intentReceived = new Intent(this, SplashActivity.class);

        String action = "";
        String title = "";
        String body = "";
        String image = "";
        int badge = 0;
        if (remoteMessage.getData() != null) {
            for (String key : remoteMessage.getData().keySet()) {
                switch (key) {
                    case "title" -> {
                        title = remoteMessage.getData().get("title");
                    }
                    case "body" -> {
                        body = remoteMessage.getData().get("body");
                    }
                    case "image" -> {
                        image = remoteMessage.getData().get("image");
                    }
                    case "action" -> {
                        action = remoteMessage.getData().get("action");
                    }
                    case "badge" -> {
                        badge = Integer.parseInt(remoteMessage.getData().get("badge"));
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case "ONIBUS" -> {
                    Passageiro passageiro = new Passageiro();
                    JSONObject jsonObjectPassageiro = null;
                    try {
                        Log.e("TAG", "onMessageReceived: " + body);
                        jsonObjectPassageiro = new JSONObject(body);
                        passageiro.setId(jsonObjectPassageiro.getInt("passageiro"));
                        passageiro.setEmbarcou(jsonObjectPassageiro.getBoolean("embarcou"));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Notification sNotification = new Notification();
                    intentReceived.putExtra("EMBARCOU", "EMBARCOU");
                    sNotification.showNotification(getApplicationContext(), title, " Boa Viagem!!!", intentReceived);
                    Intent intent = new Intent();
                    intent.setAction("MOTORISTA-PUSH");
                    intent.putExtra("PASSAGEIRO", passageiro);
                    sendBroadcast(intent);

                }

                case "PROXIMO" -> {
                    JSONObject jsonObjecTProximo = null;
                    try {

                        jsonObjecTProximo = new JSONObject(body);

                        String motorista = jsonObjecTProximo.getString("motorista");
                        String modelo = jsonObjecTProximo.getString("modelo");
                        String placa = jsonObjecTProximo.getString("placa");
                        double distancia = jsonObjecTProximo.getDouble("distancia");
                        int metros = (int) distancia;

                     //   SNotification sNotification = new SNotification(this);
                        Notification sNotification = new Notification();
                        sNotification.showNotification(getApplicationContext(),title, motorista + " " + modelo + " "  + placa +  " " + metros + " metros", intentReceived);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                case "CHEGOU" -> {
                    Notification sNotification = new Notification();
                    intentReceived.putExtra("NOTIFICA", "NOTIFICA_CHEGOU");
                    sNotification.showNotification(getApplicationContext(), title, body, intentReceived);

                    Intent intent = new Intent();
                    intent.setAction("MOTORISTA-CHEGOU");
                    sendBroadcast(intent);
                }

                case "VIAGEM_CANCELADA" -> {
                    SNotification sNotification = new SNotification(this);
                    sNotification.sendNotification(title, body, image, action, badge);
                    Intent intent = new Intent();
                    intent.setAction("VIAGEM_CANCELADA");
                    sendBroadcast(intent);
                }

                case "PIX_APROVADO" -> {
                    Log.e("FCM", "pix aprovado 1");

                    SNotification sNotification = new SNotification(this);
                    sNotification.sendNotification(title, body, image, action, badge);
                    Intent intent = new Intent();
                    intent.setAction("PIX_APROVADO");
                    sendBroadcast(intent);



                    Log.e("FCM", "pix aprovado 3");
                }


            }
        } else {
         //   SNotification sNotification = new SNotification(this);
         //   Notification sNotification = new Notification();
         //   sNotification.showNotification(getApplicationContext(),title, body,intentReceived);
        }
    }
}