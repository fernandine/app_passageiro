package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose
import java.io.Serializable

class MotoristaViagem : Serializable {

    @Expose
    var motorista =""
    @Expose
    var placa = ""
    @Expose
    var modelo = ""
    @Expose
    var marca = ""
    @Expose
    var foto = ""


}