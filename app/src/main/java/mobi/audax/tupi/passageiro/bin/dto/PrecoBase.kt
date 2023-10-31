package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class PrecoBase {

    @Expose
    var precoViagem = 0.0
    @Expose
    var precoMinutoRodado = 0.0
    @Expose
    var precoQuilometroRodado = 0.0
    @Expose
    var taxaCancelamento = 0.0
    @Expose
    var percentualCobradoCorrida = 0.0
    @Expose
    var minutosCancelamento = 0

}