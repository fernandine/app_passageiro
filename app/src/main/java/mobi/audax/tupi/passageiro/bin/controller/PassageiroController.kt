package mobi.audax.tupi.passageiro.bin.controller

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Cartao
import mobi.audax.tupi.passageiro.bin.bean.Passenger
import mobi.audax.tupi.passageiro.bin.bo.CartaoBo
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo
import mobi.audax.tupi.passageiro.bin.controller.retrofit.PassageiroInterface
import mobi.audax.tupi.passageiro.bin.dto.GoogleAuth
import mobi.audax.tupi.passageiro.bin.dto.Hash
import mobi.audax.tupi.passageiro.bin.dto.Login
import mobi.audax.tupi.passageiro.bin.dto.PassageiroUpdateRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PassageiroController(override val context: Context) : CommonsController(context) {

    fun login(login: Login, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.login(login)
        call.enqueue(object : Callback<Passenger> {
            override fun onResponse(call: Call<Passenger>, response: Response<Passenger>) {
                if (response.code() == 200) {
                    PassengerBo(context).insert(response.body())
                }
                onComplete(response.code())
            }

            override fun onFailure(call: Call<Passenger>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun googleLogin(auth: GoogleAuth, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.googleAuth(auth)
        call.enqueue(object : Callback<Passenger> {
            override fun onResponse(call: Call<Passenger>, response: Response<Passenger>) {
                if (response.code() == 200) {
                    PassengerBo(context).insert(response.body())
                }
                onComplete(response.code())
            }

            override fun onFailure(call: Call<Passenger>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun create(passenger: Passenger, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.create(passenger)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onComplete(response.code())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun update(pur: PassageiroUpdateRequest, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.update(pur)
        call.enqueue(object : Callback<Passenger> {
            override fun onResponse(call: Call<Passenger>, response: Response<Passenger>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        update(pur, onBefore, onComplete)
                    }
                } else {
                    if (response.code() == 200) {
                        val entity = response.body()
                        entity?.token = jwt
                        PassengerBo(context).insert(entity)
                    }
                    onComplete(response.code())
                }
            }

            override fun onFailure(call: Call<Passenger>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun updatePaymentMethod(id: Int, cartao: Cartao, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.updatePaymentMethod(id, hash, cartao)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        updatePaymentMethod(id, cartao, onBefore, onComplete)
                    }
                } else {
                    if (response.code() == 200) {
                        cartao.id = id
                        CartaoBo(context).update(cartao)
                    }
                    onComplete(response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun refreshToken(onRefreshed: () -> Unit) {
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.refreshToken(Hash(this.hash))
        call.enqueue(object : Callback<Passenger> {
            override fun onResponse(call: Call<Passenger>, response: Response<Passenger>) {
                val passengerBo = PassengerBo(context)
                when (response.code()) {
                    201 -> passengerBo.insert(response.body())
                    else -> passengerBo.logout()
                }
                onRefreshed()
            }

            override fun onFailure(call: Call<Passenger>, t: Throwable) {
                onRefreshed()
            }
        })
    }

    fun recuperarSenha(email: String, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PassageiroInterface::class.java)
        val call = service.recuperarSenha(email)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onComplete(response.code())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

}