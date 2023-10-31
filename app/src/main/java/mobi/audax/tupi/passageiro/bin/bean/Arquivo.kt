package mobi.audax.tupi.passageiro.bin.bean

import com.google.gson.annotations.Expose
import mobi.stos.podataka_lib.annotations.Entity
import mobi.stos.podataka_lib.annotations.PrimaryKey
import java.io.Serializable

@Entity
class Arquivo : Serializable {

    @Expose(serialize = false, deserialize = true)
    @PrimaryKey(autoIncrement = false)
    var id : Int = 0
    @Expose
    var caminho: String? = ""

}