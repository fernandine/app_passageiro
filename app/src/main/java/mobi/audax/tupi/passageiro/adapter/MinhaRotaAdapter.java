package mobi.audax.tupi.passageiro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.dto.Coordenadas;
import mobi.audax.tupi.passageiro.bin.dto.MinhaRota;


public class MinhaRotaAdapter extends ArrayAdapter<MinhaRota> {
    private static final String TAG = "MinhaRotaAdapter";


    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MinhaRotaAdapter(@NonNull Context context, @NonNull List<MinhaRota> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.adapter_historico_rotas, parent, false);

        int counter = 0;

        MinhaRota minhaRota = getItem(position);
        ImageView ivMapa = root.findViewById(R.id.imageViewMapa);
        TextView tvPartida = root.findViewById(R.id.textViewLocalDePartida);
        TextView tvDestino = root.findViewById(R.id.textViewDestino);
        TextView tvData = root.findViewById(R.id.textViewData);
        TextView tvHorario = root.findViewById(R.id.textViewHora);

        if (minhaRota.getEmbarcadoAt()!=0){
            Date data = new Date(minhaRota.getEmbarcadoAt());
            String dataFormatada = sdf.format(data);
            String horaFormatada = sdfHora.format(data);
            tvData.setText(dataFormatada);
            tvHorario.setText(horaFormatada);
        }
        else{
            tvData.setText("");
            tvHorario.setText("");
        }


        tvPartida.setText(minhaRota.getPartida());
        tvDestino.setText(minhaRota.getDestino());

        StringBuilder sb = new StringBuilder();
        Log.e(TAG, "getView NOME MOTORISTAAAAAAAAAAAAAA: "+minhaRota.getNomeMotorista() );

        for (Coordenadas listCoordenada : minhaRota.getListCoordenadas()) {
            sb.append("&waypoint").append(counter).append("=").append(listCoordenada.getLatitude()).append(",").append(listCoordenada.getLongitude());

            if (counter+1 == minhaRota.getListCoordenadas().size()){
                sb.append("&poix").append(counter).append("=").append(listCoordenada.getLatitude()).append(",").append(listCoordenada.getLongitude()).append(";red;red;11;.");
            }
           else if (counter == 0) {
                sb.append("&poix").append(counter).append("=").append(listCoordenada.getLatitude()).append(",").append(listCoordenada.getLongitude()).append(";00a3f2;00a3f2;11;.");
            }
            else{
                sb.append("&poix").append(counter).append("=").append(listCoordenada.getLatitude()).append(",").append(listCoordenada.getLongitude()).append(";white;white;11;.");
            }
            counter++;

        }

        sb.append("&lc=1652B4"); //Line color
        sb.append("&lw=4"); //Line width
        sb.append("&t=2"); //Map scheme type -- 0 (normal.day),1 (satellite.day)..
        sb.append("&ppi=250"); //Resolution to be used.
        sb.append("&w=380");//Result image width in pixels, maximum 2048.
        sb.append("&h=200"); //Result image height in pixels, maximum 2048.
        sb.append("&z=20"); // zoom, default é 20

        minhaRota.setImagemMapa(sb.toString());
        Picasso.get().load(getContext().getString(R.string.heremaps_rotas,sb.toString())).into(ivMapa);

        Log.e(TAG, "getView adapteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeer: "+getContext().getString(R.string.heremaps_rotas,sb.toString()) );


        return root;
    }

}
