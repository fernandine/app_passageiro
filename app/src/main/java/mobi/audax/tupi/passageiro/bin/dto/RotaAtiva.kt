package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class RotaAtiva {

    @Expose
    var id = 0
    @Expose
    var nome : String = ""
    @Expose
    var desembarcou : Boolean = false
    @Expose
    var embarcou : Boolean = false
    @Expose
    var passagemCancelada : Boolean = false
    @Expose
    var desembarque : Desembarque? = null
    @Expose
    var embarque : Embarque? = null
    @Expose
    var motoristaViagem : MotoristaViagem? = null

}