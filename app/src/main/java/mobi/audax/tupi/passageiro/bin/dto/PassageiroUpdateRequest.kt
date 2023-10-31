package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose
import java.io.Serializable

class PassageiroUpdateRequest : Serializable {

    @Expose
    var id = 0
    @Expose
    var nome = ""
    @Expose
    var email = ""
    @Expose
    var celular = ""
    @Expose
    var cidade = ""
    @Expose
    var cep = 0
    @Expose
    var senha: String? = null
    @Expose
    var foto: String? = null

}