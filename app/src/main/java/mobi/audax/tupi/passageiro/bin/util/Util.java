package mobi.audax.tupi.passageiro.bin.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.InputMismatchException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.callback.DialogSimpleCallback;

public class Util {
    public static final int IMG_WIDTH_HD = 1920;
    public static final int IMG_HEIGHT_HD = 1080;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    public static boolean isMyServiceRunning(@NotNull Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static double haversine(double orglat, double orglon, double destlat, double destlon) {
        orglat = orglat * Math.PI / 180;
        orglon = orglon * Math.PI / 180;
        destlat = destlat * Math.PI / 180;
        destlon = destlon * Math.PI / 180;

        int raioterra = 6378140; // METROS
        double dlat = destlat - orglat;
        double dlon = destlon - orglon;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(orglat) * Math.cos(destlat) * Math.pow(Math.sin(dlon / 2), 2);
        double distancia = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return raioterra * distancia;
    }


    public static int diferenceDates(Date date1, Date date2) throws Exception {

        if (date1.after(date2)) {
            throw new Exception("O segundo parâmetro deve ser maior que o primeiro.");
        }

        long differenceMilliSeconds = date2.getTime() - date1.getTime();
        float r = (differenceMilliSeconds / 1000 / 60 / 60 / 24);
        return Math.round(r);

    }

    public static String zeroFill(Object valor, int zeros) {
        String sValor = String.valueOf(valor);

        if (sValor.length() > zeros) {
            return sValor;
        }

        int restantes = zeros - sValor.length();
        String zadd = "";
        for (int i = 0; i < restantes; i++) {
            zadd += "0";
        }

        return zadd.concat(sValor);
    }

    private static boolean dirChecker(String name) {
        return new File(name).mkdir();
    }

    public static String age(Date dataNasc) {
        Calendar dateOfBirth = new GregorianCalendar();
        dateOfBirth.setTime(dataNasc);

        Calendar today = Calendar.getInstance();
        long tempo = today.getTimeInMillis() - dateOfBirth.getTimeInMillis();
        float r = (tempo / 1000 / 60 / 60 / 24);
        int produto = Math.round(r);

        System.out.println("produto: " + produto);

        int anos = 0;
        int meses;

        if (produto >= 365) {
            anos = produto / 365;
            meses = anos / 12;
        } else if (produto >= 30) {
            meses = produto / 30;
        } else {
            return produto + " dias";
        }

        StringBuilder sb = new StringBuilder();
        if (anos > 0) {
            sb.append(anos);
            sb.append(" ano");
            if (anos > 1) {
                sb.append("s");
            }
        }
        if (meses > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(meses);
            if (meses > 1) {

                sb.append(" meses");
            } else {
                sb.append(" mês");
            }
        }
        return sb.toString();
    }

    @NonNull
    public static String jsonToString(String path) {
        StringBuilder builder = new StringBuilder();
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(path);
            br = new BufferedReader(fr);

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                builder.append(sCurrentLine);
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }

        return builder.toString();
    }

    /**
     * Função redimensiona a imagem em um tamanho menor.
     */
    public static Bitmap redimensionarImagem(Bitmap bitmap, int width, int height) {
        try {
            if (width == bitmap.getWidth() && height == bitmap.getHeight()) {
                return bitmap;
            }

            if (bitmap.getWidth() > bitmap.getHeight()) { // a imagem está rodada então inverter w por h
                int w = width;
                int h = height;

                height = w;
                width = h;
            }

            int imgW = bitmap.getWidth();
            int imgH = bitmap.getHeight();
            if (width > imgW && height > imgH) {
                width = imgW;
                height = imgH;
            } else {
                double scale1 = Double.parseDouble(String.valueOf(width)) / Double.parseDouble(String.valueOf(imgW));
                double scale2 = Double.parseDouble(String.valueOf(height)) / Double.parseDouble(String.valueOf(imgH));
                double scale = (scale1 > scale2) ? scale2 : scale1;

                Long w = Math.round(imgW * scale);
                Long h = Math.round(imgH * scale);

                width = w.intValue();
                height = h.intValue();
            }

            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Não foi possível redimencionar o arquivo!");
            return bitmap;
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            var imageFile = new File(imagePath);
            var exif = new ExifInterface(imageFile.getAbsolutePath());
            var orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270;
                case ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180;
                case ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90;
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static void alert(Context context, int title, int message) {
        alert(context, context.getString(title), context.getString(message), null, null);
    }

    public static void alert(Context context, int title, int message, int button) {
        alert(context, context.getString(title), context.getString(message), context.getString(button), null);
    }

    public static void alert(Context context, int title, int message, DialogSimpleCallback callback) {
        alert(context, context.getString(title), context.getString(message), null, callback);
    }

    public static void alert(Context context, int title, int message, int button, DialogSimpleCallback callback) {
        alert(context, context.getString(title), context.getString(message), context.getString(button), callback);
    }

    public static void alert(Context context, int title, int message, int button, DialogSimpleCallback callback, int button2, DialogSimpleCallback callback2) {
        alert(context, context.getString(title), context.getString(message), context.getString(button), callback, context.getString(button2), callback2);
    }

    public static void alert(Context context, int title, int message, int button, DialogSimpleCallback callback, int button2) {
        alert(context, context.getString(title), context.getString(message), context.getString(button), callback, context.getString(button2), null);
    }

    public static void alert(Context context, int title, String message, int button, DialogSimpleCallback callback, int button2) {
        alert(context, context.getString(title), message, context.getString(button), callback, context.getString(button2), null);
    }

    public static void alert(Context context, String title, String message) {
        alert(context, title, message, null, null);
    }

    public static void alert(Context context, int title, String message) {
        alert(context, context.getString(title), message, null, null);
    }

    public static void alert(Context context, String title, String message, String button) {
        alert(context, title, message, button, null);
    }

    public static void alert(Context context, String title, String message, String buttton, DialogSimpleCallback callback) {
        alert(context, title, message, buttton, callback, null, null);
    }

    public static void alert(Context context, String title, String message, String buttton, DialogSimpleCallback callback, String button2, DialogSimpleCallback callback2) {
        if (TextUtils.isEmpty(buttton)) {
            buttton = context.getString(R.string.fechar);
        }

        Activity activity = null;
        var isFinishing = false;
        if (context instanceof Activity) {
            activity = (Activity) context;
            isFinishing = activity.isFinishing();
        }
        if (!isFinishing) {
            if (activity == null) {
                // region default alert dialog
                var dialog = new AlertDialog.Builder(context).create();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, buttton, (dialog13, which) -> {
                    dialog13.dismiss();
                    if (callback != null) {
                        callback.onCallback();
                    }
                });
                if (button2 != null) {
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, button2, (dialog1, which) -> {
                        dialog1.dismiss();
                        if (callback2 != null) {
                            callback2.onCallback();
                        }
                    });
                    dialog.setOnShowListener(dialog1 -> {
                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    });
                }
                dialog.show();
                // endregion
            } else {
                // region alert modificado
                var view = activity.getLayoutInflater().inflate(R.layout.dialog_alert, null);
                ((TextView) view.findViewById((R.id.title))).setText(title);
                ((TextView) view.findViewById((R.id.body))).setText(message);
                LinearLayout buttonLayout = view.findViewById(R.id.linearLayoutButton);
                buttonLayout.setWeightSum(2);
                var dialogButtonCancel = (Button) view.findViewById(R.id.dialogButtonCancel);
                var dialogButtonConfirm = (Button) view.findViewById(R.id.dialogButtonConfirm);

                dialogButtonConfirm.setText(buttton);

                if (StringUtils.isBlank(button2)) {
                    dialogButtonCancel.setVisibility(View.GONE);
                } else {
                    dialogButtonCancel.setText(button2);
                }

                var builder = new AlertDialog.Builder(context);
                builder.setView(view);
                var dialog = builder.create();
                dialogButtonConfirm.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callback != null) {
                        callback.onCallback();
                    }
                });
                dialogButtonCancel.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callback2 != null) {
                        callback2.onCallback();
                    }
                });
                view.findViewById(R.id.sair).setOnClickListener(v -> dialog.dismiss());

                dialog.setCancelable(true);
                dialog.show();
                // endregion
            }
        }
    }

    public static int getCameraPhotoOrientation(Bitmap bitmap) {
        int rotate = 0;
        try {
            var bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            var data = bos.toByteArray();
            var bs = new ByteArrayInputStream(data);
            var exif = new ExifInterface(bs);

            var orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270;
                case ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180;
                case ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90;
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    @Nullable
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        } else {
            try {
                var bmOptions = new BitmapFactory.Options();
                var bitmap = BitmapFactory.decodeFile(path, bmOptions);
                bitmap = Util.rotateBitmap(bitmap, Util.getCameraPhotoOrientation(path));
                var bitmapRedimensionada = Util.redimensionarImagem(bitmap, 1920, 1080);
                var byteArrayOS = new ByteArrayOutputStream();
                bitmapRedimensionada.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
                return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        var byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP);
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }


    public static JSONArray pathToBase64(String paths) {
        JSONArray jsonArray = new JSONArray();
        if (StringUtils.isNotBlank(paths)) {
            try {
                paths = paths.replaceAll("\\[", "").replaceAll("]", "");
                String[] array = paths.split(",");
                for (String path : array) {
                    path = path.trim();
                    if (StringUtils.isNotBlank(path)) {
                        jsonArray.put(fileToBase64(new File(path)));
                    }
                }
            } catch (Exception e) {
                //
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    public static void deleteFiles(String filePaths) {
        filePaths = filePaths.replaceAll("\\[", "").replaceAll("]", "");
        String[] array = filePaths.split(",");
        for (String path : array) {
            path = path.trim();
            if (StringUtils.isNotBlank(path)) {
                File file = new File(path);
                if (!file.getName().contains("CAMINHAO")) {
                    file.delete();
                }
            }
        }
    }


    public static void deleteFotosAntigas() {
        String path = Environment.getExternalStorageDirectory().toString() + "/.NUTCININDUSTRIA";
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files != null) {

            for (File file : files) {
                if (file.getName().contains("CAMINHAO") && ((new Date().getTime() - file.lastModified()) > TimeUnit.HOURS.toMillis(1))) {
                    file.delete();
                } else if (file.getName().contains("IMG") && ((new Date().getTime() - file.lastModified()) > TimeUnit.HOURS.toMillis(7))) {
                    {
                        file.delete();
                    }
                }

            }

        }
    }

    public static void dismissProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {

                //get the Context object that was used to create the dialog
                Context context = ((ContextWrapper) progressDialog.getContext()).getBaseContext();

                // if the Context used here was an activity AND it hasn't been finished or destroyed
                // then dismiss it
                if (context instanceof Activity) {

                    // Api >=17
                    if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                        dismissWithExceptionHandling(progressDialog);
                    }
                } else
                    // if the Context used wasn't an Activity, then dismiss it too
                    dismissWithExceptionHandling(progressDialog);
            }
            progressDialog = null;
        }
    }

    public static void dismissWithExceptionHandling(ProgressDialog dialog) {
        try {
            dialog.dismiss();
        } catch (final IllegalArgumentException e) {
            // Do nothing.
        } catch (final Exception e) {
            // Do nothing.
        } finally {
            dialog = null;
        }
    }

    public static String fileToBase64(File photoFile) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        bmOptions.inDither = true;
        Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
        myBitmap = Util.rotateBitmap(myBitmap, Util.getCameraPhotoOrientation(photoFile.getAbsolutePath()));
        myBitmap = Util.redimensionarImagem(myBitmap, 480, 720);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        return Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
    }

    @NonNull
    public static byte[] readFile(File file) {
        try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @NonNull
    public static String onlyNumber(String s) {
        var unmask = new StringBuilder();
        if (s != null) {
            for (int i = 0; i < s.length(); i++) {
                if (Character.isDigit(s.charAt(i))) {
                    unmask.append(s.charAt(i));
                }
            }
        }
        return unmask.toString();
    }

    public static boolean validateEmail(String emailStr) {
        var matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }


    public static void showAlertDialog(String title, String mensagem, @NonNull Activity activity, @Nullable DialogSimpleCallback callback) {
        var view = activity.getLayoutInflater().inflate(R.layout.dialog_rest, null);

        var alertDialog = new AlertDialog.Builder(activity);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.body)).setText(mensagem);
        var button = (Button) view.findViewById(R.id.dialogButton);
        button.setText(activity.getString(R.string.ok));
        alertDialog.setView(view);

        var dialog = alertDialog.create();
        view.findViewById(R.id.sair_dialog).setOnClickListener(v -> dialog.dismiss());
        button.setOnClickListener(v -> {
            if (callback != null) {
                callback.onCallback();
            }
            dialog.dismiss();
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public static void showAlertDialog(String mensagem, @NonNull Activity activity, @Nullable DialogSimpleCallback callback) {
        showAlertDialog(null, mensagem, activity, callback);
    }

    public static void showAlertDialog(String title, String mensagem, @NonNull Activity activity) {
        showAlertDialog(title, mensagem, activity, null);
    }

    public static void showAlertDialog(String mensagem, @NonNull Activity activity) {
        showAlertDialog(mensagem, activity, null);
    }

    public static void loginExpirado(@NonNull Activity activity, DialogSimpleCallback callback) {
        showAlertDialog(activity.getString(R.string.login_expirado), activity, callback);
    }

    public static void loginExpirado(@NonNull Activity activity) {
        showAlertDialog(activity.getString(R.string.login_expirado), activity, null);
    }

    public static boolean isCPF(String CPF) {
        CPF = onlyNumber(CPF);
        if (CPF.equals("00000000000") || CPF.equals("11111111111")
                || CPF.equals("22222222222") || CPF.equals("33333333333")
                || CPF.equals("44444444444") || CPF.equals("55555555555")
                || CPF.equals("66666666666") || CPF.equals("77777777777")
                || CPF.equals("88888888888") || CPF.equals("99999999999")
                || (CPF.length() != 11)) {
            return false;
        }
        char dig10, dig11;
        int sm, i, r, num, peso;

        try {
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }
            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig10 = '0';
            } else {
                dig10 = (char) (r + 48);
            }
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig11 = '0';
            } else {
                dig11 = (char) (r + 48);
            }

            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10))) {
                return true;
            } else {
                return false;
            }
        } catch (InputMismatchException erro) {
            return false;
        }

    }

    public static boolean isCNPJ(String CNPJ) {
        CNPJ = onlyNumber(CNPJ);
        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") ||
                CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") ||
                CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555") ||
                CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") ||
                CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999") ||
                (CNPJ.length() != 14))
            return (false);

        char dig13, dig14;
        int sm, i, r, num, peso;

        try {
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig13 = '0';
            else dig13 = (char) ((11 - r) + 48);

            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig14 = '0';
            else dig14 = (char) ((11 - r) + 48);

            return (dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13));
        } catch (InputMismatchException erro) {
            return (false);
        }
    }


}
