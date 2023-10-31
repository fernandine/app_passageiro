package mobi.audax.tupi.passageiro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.dto.MinhaRota;
import mobi.audax.tupi.passageiro.bin.dto.Pagamento;

public class PagamentoAdapter extends ArrayAdapter<Pagamento> {

    public PagamentoAdapter(@NonNull Context context, List<Pagamento> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_pagamento, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        var entity = getItem(position);
        if (entity != null) {
            holder.label.setText(entity.getLabel());
            switch (entity.getPaymentTypeEnum()) {
                case PIX -> holder.icon.setImageResource(R.drawable.ic_pix);
                case DINHEIRO -> holder.icon.setImageResource(R.drawable.ic_dinheiro);
                default -> holder.icon.setImageResource(R.drawable.baseline_credit_card_24);
            }
        }
        return view;
    }

    private static class ViewHolder {
        final TextView label;
        final ImageView icon;

        public ViewHolder(@NonNull View view) {
            this.label = view.findViewById(R.id.label);
            this.icon = view.findViewById(R.id.icon);
        }
    }

}
