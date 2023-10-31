package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class Link {

    @Expose
    var rel: String? = null
    @Expose
    var href: String = "" //imagem do qrcode
    @Expose
    var media: String? = null
    @Expose
    var type: String? = null
}