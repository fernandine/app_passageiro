package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose
import java.io.Serializable

class Hash : Serializable {

    @Expose
    var hash : String? = null

    constructor()

    constructor(hash: String?) {
        this.hash = hash
    }
}