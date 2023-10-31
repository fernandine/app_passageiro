package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class ParadaDestino {

    @Expose
    var latitude = 0.0
    @Expose
    var longitude = 0.0
    @Expose
    var passageiro = 0
    @Expose
    var destino : Coordenadas? = null

}