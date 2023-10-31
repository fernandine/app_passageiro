package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class Login {

    @Expose
    var email : String = ""
    @Expose
    var senha : String = ""
    @Expose
    var pushId : String? = ""

    constructor()

    constructor(email : String, senha: String, pushId: String?) {
        this.email = email
        this.senha = senha
        this.pushId = pushId
    }

}