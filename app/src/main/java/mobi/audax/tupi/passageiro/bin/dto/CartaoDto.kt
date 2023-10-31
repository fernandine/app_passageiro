package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class CartaoDto {

    @Expose
    var tipoCartao = 0

    @Expose
    var mesExpiracao: String = ""

    @Expose
    var anoExpiracao: String = ""

    @Expose
    var holder: String = ""

//    @Expose
//    var numero: String = ""

    @Expose
    var cvv: String = ""

    @Expose
    var creditCardNumber: String = ""

    @Expose
    var creditCardBrand: String = ""

    @Expose
    var defaultPayment: Boolean = false

}