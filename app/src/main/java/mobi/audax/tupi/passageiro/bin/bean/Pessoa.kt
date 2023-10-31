package mobi.audax.tupi.passageiro.bin.bean

import com.google.gson.annotations.Expose
import mobi.stos.podataka_lib.annotations.Entity
import mobi.stos.podataka_lib.annotations.ForeignKey
import mobi.stos.podataka_lib.annotations.PrimaryKey
import java.io.Serializable

@Entity
class Pessoa : Serializable {

    @Expose(serialize = false, deserialize = true)
    @PrimaryKey(autoIncrement = false)
    var id: Int = 0
    @Expose
    @ForeignKey
    var fotoPerfil: Arquivo? = null
    @Expose
    var nome: String? = ""
    @Expose
    var cpf: String? = ""
    @Expose
    var celular: String? = ""
    @Expose
    var cep: Int = 0

}