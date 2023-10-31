package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class ParadaEmbarque {

    @Expose
    var destino: ParadaProxima? = null
    @Expose
    var embarque: ParadaProxima? = null
    @Expose
    var posicaoMotorista : Coordenadas? = null
    @Expose
    var viagem =  0
    @Expose
    var tempoMotoristaParada = 0
    @Expose
    var tempoParadaDestino = 0L
    @Expose
    var distanciaParadaDestino = 0.0
    @Expose
    var valorPassagem = 0.0
    @Expose
    var motoristaViagem : MotoristaViagem? = null

}