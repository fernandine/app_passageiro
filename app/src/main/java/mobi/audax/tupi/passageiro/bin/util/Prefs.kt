package mobi.audax.tupi.passageiro.bin.util

import android.content.Context
import android.location.Location
import mobi.audax.tupi.passageiro.bin.task.GeoDecodeTask
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class Prefs(val context: Context) {
    val prefs = context.getSharedPreferences("Tupi", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    private fun md5Hex(key : String) : String  {
        return String(Hex.encodeHex(DigestUtils.md5(key)))
    }
    var endpoint: String
        get() {
            val http = prefs.getString(md5Hex("HTTP_ENDPOINT"), null)
            return http ?: ""
        }
        set(value)  {
            var text = value
            if (!value.endsWith("/")) {
                text += "/"
            }
            editor.putString(md5Hex("HTTP_ENDPOINT"), text).apply()
        }
    var appLock : Boolean
        get() = prefs.getBoolean(md5Hex("APP_LOCK"), false)
        set(value) { editor.putBoolean(md5Hex("APP_LOCK"), value).apply() }
    var isDatabaseDeprecated : Boolean
        get() {
            val dif = Util.diferenceDates(lastDatabaseUpdate, Date())
            return dif >= 1
        }
        set(value) {}
    var isPrimeiroAcesso: Boolean
        get() = prefs.getBoolean(md5Hex("ISPRIMEIROACESSO"), true)
        set(value) { editor.putBoolean(md5Hex("ISPRIMEIROACESSO"), value).apply() }
    var lastDatabaseUpdate: Date
        get() = Date(prefs.getLong(md5Hex("LAST_DB_UPDATE"), 0))
        set(value) { editor.putLong(md5Hex("LAST_DB_UPDATE"), value.time).apply() }
    var logradouro: String?
        get() = prefs.getString(md5Hex("LOGRADOURO"), null)
        set(value) { editor.putString(md5Hex("LOGRADOURO"), value).apply() }
    var numero: String?
        get() = prefs.getString(md5Hex("NUMERO"), null)
        set(value) { editor.putString(md5Hex("NUMERO"), value).apply() }
    var bairro: String?
        get() = prefs.getString(md5Hex("BAIRRO"), null)
        set(value) { editor.putString(md5Hex("BAIRRO"), value).apply() }
    var cidade: String?
        get() = prefs.getString(md5Hex("CIDADE"), null)
        set(value) { editor.putString(md5Hex("CIDADE"), value).apply() }
    var uf: String?
        get() = prefs.getString(md5Hex("UF"), null)
        set(value) { editor.putString(md5Hex("UF"), value).apply() }
    var cep: String?
        get() = prefs.getString(md5Hex("CEP"), null)
        set(value) { editor.putString(md5Hex("CEP"), value).apply() }
    var latitude: Float
        get() = prefs.getFloat(md5Hex("LATITUDE"), 0f)
        set(value) { editor.putFloat(md5Hex("LATITUDE"), value).apply() }
    var longitude: Float
        get() = prefs.getFloat(md5Hex("LONGITUDE"), 0f)
        set(value) { editor.putFloat(md5Hex("LONGITUDE"), value).apply() }
    var precisao: Float
        get() = prefs.getFloat(md5Hex("PRECISAO"), 0f)
        set(value) { editor.putFloat(md5Hex("PRECISAO"), value).apply() }
    var velocidade: Float
        get() = prefs.getFloat(md5Hex("VELOCIDADE"), 0f)
        set(value) { editor.putFloat(md5Hex("VELOCIDADE"), value).apply() }
    var bearing: Float
        get() = prefs.getFloat(md5Hex("BEARING"), 0f)
        set(value) { editor.putFloat(md5Hex("BEARING"), value).apply() }
    var satellites: Int
        get() = prefs.getInt(md5Hex("SATELLITES"), 0)
        set(value) { editor.putInt(md5Hex("SATELLITES"), value).apply() }
    var viagemId: Int
        get() = prefs.getInt(md5Hex("VIAGEMID"), 0)
        set(value) { editor.putInt(md5Hex("VIAGEMID"), value).apply() }
    var token : String?
        get() = prefs.getString(md5Hex("TOKEN"), "")
        set(value) { editor.putString(md5Hex("TOKEN"), value).apply() }

    var embarcou : Boolean
        get() = prefs.getBoolean(md5Hex("EMBARCOU"), false)
        set(value) { editor.putBoolean(md5Hex("EMBARCOU"), value).apply() }

    var desembarcou : Boolean
        get() = prefs.getBoolean(md5Hex("DESEMBARCOU"), false)
        set(value) { editor.putBoolean(md5Hex("DESEMBARCOU"), value).apply() }

    fun recordLocation(location : Location) {
        this.latitude = location.latitude.toFloat()
        this.longitude = location.longitude.toFloat()
        this.precisao = location.accuracy
        this.velocidade = location.speed * 3.6f
        this.bearing = location.bearing

        location.extras?.let {
            if (it.containsKey("satellites")) {
                this.satellites = it.getInt("satellites")
            }
        }
        GeoDecodeTask(context, location).decoder { }
    }

}