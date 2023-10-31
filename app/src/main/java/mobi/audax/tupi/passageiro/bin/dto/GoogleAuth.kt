package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class GoogleAuth {

    @Expose
    var fireBaseId: String? = ""
    @Expose
    var pushId: String? = ""

    constructor()

    constructor(fireBaseId: String?, pushId: String?) {
        this.fireBaseId = fireBaseId
        this.pushId = pushId
    }


}