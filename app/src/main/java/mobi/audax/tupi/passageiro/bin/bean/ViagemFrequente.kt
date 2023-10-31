package mobi.audax.tupi.passageiro.bin.bean

import mobi.stos.podataka_lib.annotations.Entity
import mobi.stos.podataka_lib.annotations.PrimaryKey
import java.io.Serializable

@Entity
class ViagemFrequente : Serializable {
    @PrimaryKey(autoIncrement = false)
    var id : Int = 0
    var latitude = 0.0
    var longitude = 0.0
    var partida: String? = null
    var destino: String? = null
}