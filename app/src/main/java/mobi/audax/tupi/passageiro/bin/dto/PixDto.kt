package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class PixDto {

    @Expose
    var id: String = ""
    @Expose
    var createdAt: String? = null
    @Expose
    var value = 0
    @Expose
    var  text: String = "" //chave pix decodificada
    @Expose
    var status: String? = null
    @Expose
    var links: List<Link> = ArrayList()
    @Expose
    var expiration_date: String? = null

    constructor()

//    @Expose
//     val qrCodes: List<QrCode>? = null
//    @Expose
//     val notificationUrls: List<String>? = null
//
//    @Expose
//    var encodedImage: String = ""
//
//    @Expose
//    var payload: String = ""
//
//    @Expose
//    var expirationDate: String = ""
}