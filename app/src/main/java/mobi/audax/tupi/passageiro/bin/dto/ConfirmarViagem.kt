package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class ConfirmarViagem {

    @Expose
    var viagem = 0
    @Expose
    var push : String? =null
    @Expose
    var valorPassagem = 0.0
    @Expose
    var passageiro = 0
    @Expose
    var paradaEmbarque = 0
    @Expose
    var paradaDesembarque = 0
    @Expose
    var paraMim = true
    @Expose
    var passageiroNaoCadastrados : MutableList<PassageiroNaoCadastrado>? = mutableListOf()

}