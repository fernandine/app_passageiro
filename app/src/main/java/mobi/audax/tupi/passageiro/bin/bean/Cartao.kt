package mobi.audax.tupi.passageiro.bin.bean

import com.google.gson.annotations.Expose
import mobi.stos.podataka_lib.annotations.Entity
import mobi.stos.podataka_lib.annotations.PrimaryKey
import java.io.Serializable

@Entity
class Cartao : Serializable {

    @Expose(serialize = false, deserialize = true)
    @PrimaryKey(autoIncrement = false)
    var id: Int = 0

    @Expose
    var tipoCartao: Int = 1

    @Expose
    var mesExpiracao: String? = ""

    @Expose
    var anoExpiracao: String? = ""

    @Expose
    var holder: String? = ""

    @Expose
    var numero: String? = ""

    @Expose
    var cvv: String? = ""

    fun ofuscado(): String {
        return "**** ${numero?.substring(numero?.length?.minus(4) ?: 12)}"
    }

}