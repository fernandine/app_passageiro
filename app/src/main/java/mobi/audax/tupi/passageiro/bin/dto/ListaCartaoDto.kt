package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class ListaCartaoDto {

    @Expose
    var id = 0

    @Expose
    var creditCardNumber: String = ""

    @Expose
    var creditCardBrand: String = ""

    @Expose
    var defaultPayment: Boolean = false

    @Expose
    var tipoCartao = 1

    fun ofuscado(): String {
        return "**** ${creditCardNumber?.substring(creditCardNumber?.length?.minus(4) ?: 12)}"
    }
}