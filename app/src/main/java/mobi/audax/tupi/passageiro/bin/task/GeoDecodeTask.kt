package mobi.audax.tupi.passageiro.bin.task

import android.content.Context
import android.location.Geocoder
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mobi.audax.tupi.passageiro.bin.util.Prefs
import org.apache.commons.lang3.StringUtils
import java.lang.Exception
import java.util.*

class GeoDecodeTask(val context: Context, val location: Location) {

    fun decoder(onDecoded: () -> Unit) {
        runBlocking {
            val job = launch(Dispatchers.Default) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]

                        val prefs = Prefs(context)
                        prefs.logradouro = address.thoroughfare
                        prefs.numero = address.subThoroughfare
                        prefs.bairro = if (address.subLocality != null) address.subLocality else address.locality
                        prefs.cidade = address.subAdminArea
                        prefs.cep = address.postalCode
                        prefs.uf = decodeShortNameState(address.adminArea)

                        onDecoded()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            job.start()
        }
    }

    private fun decodeShortNameState(state: String): String? {
        val estados: HashMap<String, String> = HashMap()
        estados["Acre"] = "AC"
        estados["Alagoas"] = "AL"
        estados["Amapá"] = "AP"
        estados["Amazonas"] = "AM"
        estados["Bahia"] = "BA"
        estados["Ceará"] = "CE"
        estados["Distrito Federal"] = "DF"
        estados["Espírito Santo"] = "ES"
        estados["Goiás"] = "GO"
        estados["Maranhão"] = "MA"
        estados["Mato Grosso"] = "MT"
        estados["Mato Grosso do Sul"] = "MS"
        estados["Minas Gerais"] = "MG"
        estados["Pará"] = "PA"
        estados["Paraíba"] = "PB"
        estados["Paraná"] = "PR"
        estados["Pernambuco"] = "PE"
        estados["Piauí"] = "PI"
        estados["Rio de Janeiro"] = "RJ"
        estados["Rio Grande do Norte"] = "RN"
        estados["Rio Grande do Sul"] = "RS"
        estados["Rondônia"] = "RO"
        estados["Roraima"] = "RR"
        estados["Santa Catarina"] = "SC"
        estados["São Paulo"] = "SP"
        estados["Sergipe"] = "SE"
        estados["Tocantins"] = "TO"
        val found = estados[state]
        return if (StringUtils.isBlank(found)) {
            state
        } else {
            found
        }
    }

}