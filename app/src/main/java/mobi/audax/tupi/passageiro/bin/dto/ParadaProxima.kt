package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose
import java.io.Serializable

class ParadaProxima : Serializable {

    @Expose
    var id = 0
    @Expose
    var nome : String? = ""
    @Expose
    var latitude = 0.0
    @Expose
    var longitude = 0.0

}