package mobi.audax.tupi.passageiro.bin.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaskCpf {

    public static String unmask(String s) {
        return s.replaceAll("[.]", "").replaceAll("[-]", "")
                .replaceAll("[/]", "").replaceAll("[(]", "")
                .replaceAll("[)]", "");
    }

    public static TextWatcher insert(final String mask, final EditText ediTxt) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                String str = MaskCpf.unmask(s.toString());
                String mascara = "";
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }
                int i = 0;
                for (char m : mask.toCharArray()) {
                    if (m != '#' && str.length() > old.length()) {
                        mascara += m;
                        continue;
                    }
                    try {
                        mascara += str.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                ediTxt.setText(mascara);
                ediTxt.setSelection(mascara.length());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        };
    }

    public static String addMaskPlaca(final String textoAFormatar, final String mask){
        String formatado = "";
        int i = 0;
        // vamos iterar a mascara, para descobrir quais caracteres vamos adicionar e quando...
        for (char m : mask.toCharArray()) {
            if (m != '#') { // se não for um #, vamos colocar o caracter informado na máscara
                formatado += m;
                continue;
            }
            // Senão colocamos o valor que será formatado
            try {
                formatado += textoAFormatar.charAt(i);
            } catch (Exception e) {
                break;
            }
            i++;
        }
        return formatado;
    }



}