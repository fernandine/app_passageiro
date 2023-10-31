package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class Rota {

    @Expose
    var id = 0
    @Expose
    var partida : Coordenadas? = null
    @Expose
    var destino : Coordenadas? = null
    @Expose
    var paradas : MutableList<Coordenadas>? = null
    @Expose
    var distancia = 0

}