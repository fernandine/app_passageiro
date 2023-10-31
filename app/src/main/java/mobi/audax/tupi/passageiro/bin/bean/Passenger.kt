package mobi.audax.tupi.passageiro.bin.bean

import com.google.gson.annotations.Expose
import mobi.stos.podataka_lib.annotations.Entity
import mobi.stos.podataka_lib.annotations.ForeignKey
import mobi.stos.podataka_lib.annotations.PrimaryKey
import java.io.Serializable

@Entity
class Passenger : Serializable {

    @Expose(serialize = false, deserialize = true)
    @PrimaryKey(autoIncrement = false)
    var id : Int = 0
    @Expose
    @ForeignKey
    var pessoa : Pessoa? = null
    @Expose
    @ForeignKey
    var cartao: Cartao? = null

    @Expose
    var email : String? = ""
    @Expose
    var senha : String? = ""
    @Expose
    var hash : String? = ""
    @Expose
    var token : String? = ""
    @Expose
    var cidade : String? = ""
    @Expose
    var complemento : String? = ""
    @Expose
    var fireBaseId: String? = ""
    @Expose
    var pushId: String? = ""

    init {
        if (pessoa == null) {
            pessoa = Pessoa()
        }
        if (cartao == null) {
            cartao = Cartao()
        }
        if (pessoa?.fotoPerfil == null) {
            pessoa?.fotoPerfil = Arquivo()
        }
    }

}