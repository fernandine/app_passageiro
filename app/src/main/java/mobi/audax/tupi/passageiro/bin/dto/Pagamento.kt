package mobi.audax.tupi.passageiro.bin.dto

import mobi.audax.tupi.passageiro.bin.bean.Cartao
import mobi.audax.tupi.passageiro.bin.enumm.PaymentTypeEnum
import java.io.Serializable

class Pagamento : Serializable {

    var id = 0
    var paymentTypeEnum: PaymentTypeEnum = PaymentTypeEnum.CARTAO
    var label = ""

    constructor()

    constructor(cartao: Cartao) {
        this.id = cartao.id
        this.paymentTypeEnum = PaymentTypeEnum.CARTAO
        if (cartao.tipoCartao == 1) {
            this.label = cartao.ofuscado() + " (Crédito)"
        } else {
            this.label = cartao.ofuscado() + " (Débito)"
        }
    }

    constructor(cartao: ListaCartaoDto) {
        this.id = cartao.id
        this.paymentTypeEnum = PaymentTypeEnum.CARTAO
        if (cartao.tipoCartao == 1) {
            this.label = cartao.ofuscado() + " (Crédito)"
        } else {
            this.label = cartao.ofuscado() + " (Débito)"
        }
    }

}