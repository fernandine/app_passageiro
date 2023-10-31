package mobi.audax.tupi.passageiro.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.dto.ParadaEmbarque;
import mobi.audax.tupi.passageiro.bin.util.MaskTelefone;
import mobi.audax.tupi.passageiro.bin.util.Util;

public class DuracaoRotasAdapter extends ArrayAdapter<ParadaEmbarque> {

    public DuracaoRotasAdapter(@NonNull Context context) {
        super(context, 0, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_confirmarrota, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }


        var entity = getItem(position);

        holder.desembarque.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.desembarque.setSelected(true);

        holder.nomeEmbarque.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.nomeEmbarque.setSelected(true);

        var df = new DecimalFormat("## 'min.'");
        if (entity.getTempoMotoristaParada() != 0) {
            var tempo = entity.getTempoMotoristaParada() / 60.0;
            if (tempo != 0) {
                holder.ateParada.setText(getContext().getString(R.string.embarque_em_x_minutos, df.format(tempo)));
            } else {
                holder.ateParada.setText(R.string.embarque_agora_mesmo);
            }
        } else {
            holder.ateParada.setText(R.string.voce_ja_chegou_ao_destino);
        }
        if (entity.getTempoParadaDestino() != 0) {
            var tempo = entity.getTempoParadaDestino() / 60.0;
            holder.duracaoRota.setText(df.format(tempo));
        }
        if (StringUtils.isNotBlank(entity.getEmbarque().getNome())) {
            holder.nomeEmbarque.setText(entity.getEmbarque().getNome());
        }
        if (StringUtils.isNotBlank(entity.getDestino().getNome())) {
            holder.desembarque.setText(entity.getDestino().getNome());
        }
        if (StringUtils.isNotBlank(entity.getMotoristaViagem().getPlaca())) {
            holder.placa.setText(MaskTelefone.addMaskPlaca(entity.getMotoristaViagem().getPlaca(), "###-####" ));

        }
        return view;
    }


    private static class ViewHolder {
        TextView ateParada;
        TextView desembarque;
        TextView duracaoRota;
        TextView nomeEmbarque;
        TextView placa;

        public ViewHolder(@NonNull View root) {
            this.ateParada = root.findViewById(R.id.textViewEmbarque);
            this.duracaoRota = root.findViewById(R.id.textViewTempoDuracao);
            this.desembarque = root.findViewById(R.id.textViewNomeDesembarque);
            this.nomeEmbarque = root.findViewById(R.id.textViewNomeEmbarque);
            this.placa = root.findViewById(R.id.tv_placa);
        }
    }

}
