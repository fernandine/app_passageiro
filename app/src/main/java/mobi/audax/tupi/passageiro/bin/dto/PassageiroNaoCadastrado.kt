package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class PassageiroNaoCadastrado {

    @Expose
    var nome : String? = null
    @Expose
    var documento: String? = null

    constructor()

    constructor(nome: String?, documento: String?) {
        this.nome = nome
        this.documento = documento
    }

}