package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class Pix {

    @Expose
    var valor = 0.0

    @Expose
    var hash: String = ""

    @Expose
    var pushToken: String = ""

    @Expose
    var taxId: String = ""

}