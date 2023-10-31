package mobi.audax.tupi.passageiro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchOptions;

import java.util.List;

import mobi.audax.tupi.passageiro.R;
import mobi.audax.tupi.passageiro.bin.bean.ViagemFrequente;
import mobi.audax.tupi.passageiro.bin.bean.MinhaRota;

public class ViagensFrequentesAdapter extends ArrayAdapter<MinhaRota> {
    private SearchEngine searchEngine;

    public ViagensFrequentesAdapter(@NonNull Context context, @NonNull List<MinhaRota> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.adapter_viagens_frequentes, parent, false);

        MinhaRota viagensFrequentes = getItem(position);
        TextView mTvDestino = root.findViewById(R.id.textViewDestino);
        TextView mTvPartida = root.findViewById(R.id.textViewPartida);
        mTvDestino.setText(viagensFrequentes.getDestino());
        mTvPartida.setText(viagensFrequentes.getPartida());
    //    GeoCoordinates geoCoordinates = new GeoCoordinates(viagensFrequentes.getLatitude(), viagensFrequentes.getLongitude());
    //    getAddressForCoordinates(mTvDestino,mTvPartida,geoCoordinates);

        return root;
    }

    private void getAddressForCoordinates(TextView textview1,TextView textview2,GeoCoordinates geoCoordinates) {
        int maxItems = 1;
        SearchOptions reverseGeocodingOptions = new SearchOptions(LanguageCode.EN_GB, maxItems);
        try {
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            e.printStackTrace();
        }
        searchEngine.search(geoCoordinates, reverseGeocodingOptions, (searchError, list) -> {
            if (searchError != null) {
             //   Toast.makeText(getContext(), "" + searchError.toString(), Toast.LENGTH_SHORT).show();
             //   Log.e("TAG", "searchError: " + searchError.toString());
                return;
            }
            Log.e("TAG", "getAddressForCoordinates Adapter: " + list.get(0).getAddress().addressText);

            textview1.setText(list.get(0).getAddress().street+", "+list.get(0).getAddress().houseNumOrName);
            textview2.setText(list.get(0).getAddress().city + ", " + list.get(0).getAddress().state+", "+list.get(0).getAddress().postalCode);

        });
    }
}
