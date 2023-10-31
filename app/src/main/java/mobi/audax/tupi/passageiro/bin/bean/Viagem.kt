package mobi.audax.tupi.passageiro.bin.bean

import com.google.gson.annotations.Expose
import mobi.audax.tupi.passageiro.bin.dto.MotoristaViagem
import mobi.stos.podataka_lib.annotations.Entity
import mobi.stos.podataka_lib.annotations.ForeignKey
import mobi.stos.podataka_lib.annotations.PrimaryKey
import org.apache.commons.lang3.StringUtils
import java.io.Serializable

@Entity
class Viagem : Serializable {
    @PrimaryKey(autoIncrement = false)
    var id = 0
    var nome: String? = null
    var logradouro: String? = null
    var bairro: String? = null
    var cidade: String? = null
    var cep: String? = null
    var paradaEmbarque = 0
    var paradaDesembarque = 0
    var nomeParadaDesembarque : String? = null
    var viagem = 0
    var latitudeEmbarque = 0.0
    var longitudeEmbarque = 0.0
    var latitudeDesembarque = 0.0
    var longitudeDesembarque = 0.0
    var valorPassagem = 0.0

    var motorista =""
    var placa = ""
    var modelo = ""
    var marca = ""
    var fotoMotorista = ""

    val enderecoCompleto: String
        get() {
            val builder = StringBuilder()
            if (StringUtils.isNotBlank(logradouro)) {
                builder.append(logradouro)
            }
            if (StringUtils.isNotBlank(bairro)) {
                if (builder.length > 0) {
                    builder.append(", ")
                }
                builder.append(bairro)
            }
            if (StringUtils.isNotBlank(cidade)) {
                if (builder.length > 0) {
                    builder.append(", ")
                }
                builder.append(cidade)
            }
            if (StringUtils.isNotBlank(cep)) {
                if (builder.length > 0) {
                    builder.append(", ")
                }
                builder.append(cep)
            }
            return builder.toString()
        }
}