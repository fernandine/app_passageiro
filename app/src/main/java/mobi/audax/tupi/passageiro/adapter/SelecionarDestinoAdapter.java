package mobi.audax.tupi.passageiro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.Viagem;

public class SelecionarDestinoAdapter extends ArrayAdapter<Viagem> {

    public SelecionarDestinoAdapter(@NonNull Context context) {
        super(context, 0, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_selecionar_destino, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        var entity = getItem(position);
        if (entity != null) {
            holder.nome.setText(entity.getNome());
            holder.endereco.setText(entity.getEnderecoCompleto());
        }
        return view;
    }


    private static class ViewHolder {
        final TextView nome;
        final TextView endereco;

        public ViewHolder(@NonNull View view) {
            this.nome = view.findViewById(R.id.tv1);
            this.endereco = view.findViewById(R.id.tv2);
        }
    }


}
