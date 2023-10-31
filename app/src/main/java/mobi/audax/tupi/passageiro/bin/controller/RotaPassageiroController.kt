package mobi.audax.tupi.passageiro.bin.controller

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.ViagemFrequente
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo
import mobi.audax.tupi.passageiro.bin.bo.ViagemBo
import mobi.audax.tupi.passageiro.bin.bo.ViagemFrequenteBo
import mobi.audax.tupi.passageiro.bin.controller.retrofit.RotaPassageiroInterface
import mobi.audax.tupi.passageiro.bin.dto.*
import mobi.audax.tupi.passageiro.bin.util.Prefs
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RotaPassageiroController(override val context: Context) : CommonsController(context) {

    fun viagensFrequentes(onComplete: (statusCode: Int) -> Unit) {
        val passenger = PassengerBo(context).autenticado()
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.viagensFrequentes(passenger?.id)
        call.enqueue(object : Callback<MutableList<ViagemFrequente>> {
            override fun onResponse(call: Call<MutableList<ViagemFrequente>>, response: Response<MutableList<ViagemFrequente>>) {
                if (response.code() == 401) {
                    tryRefreshToken { viagensFrequentes(onComplete) }
                } else {
                    if (response.code() == 200) {
                        ViagemFrequenteBo(context).insert(response.body())
                    }
                    onComplete(response.code())
                }
            }

            override fun onFailure(call: Call<MutableList<ViagemFrequente>>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun paradasProximas(onComplete: (statusCode: Int, MutableList<ParadaProxima>?) -> Unit) {
        val prefs = Prefs(context)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.paradasProximas(prefs.latitude.toDouble(), prefs.longitude.toDouble())
        call.enqueue(object : Callback<MutableList<ParadaProxima>> {
            override fun onResponse(call: Call<MutableList<ParadaProxima>>, response: Response<MutableList<ParadaProxima>>) {
                if (response.code() == 401) {
                    tryRefreshToken { paradasProximas(onComplete) }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<MutableList<ParadaProxima>>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

    fun paradasDestino(paradaDestino: ParadaDestino, onBefore: () -> Unit, onComplete: (statusCode: Int, MutableList<ParadaEmbarque>?) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.paradasDestino(paradaDestino)
        call.enqueue(object : Callback<MutableList<ParadaEmbarque>> {
            override fun onResponse(call: Call<MutableList<ParadaEmbarque>>, response: Response<MutableList<ParadaEmbarque>>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        paradasDestino(paradaDestino, onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<MutableList<ParadaEmbarque>>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

    fun rotaViagem(viagem: Int, onBefore: () -> Unit, onComplete: (statusCode: Int, rota: Rota?) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.rotaViagem(viagem)
        call.enqueue(object : Callback<Rota> {
            override fun onResponse(call: Call<Rota>, response: Response<Rota>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        rotaViagem(viagem, onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<Rota>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

    fun precoBase(onComplete: (statusCode: Int, precoBase: PrecoBase?) -> Unit) {
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.precoBase()
        call.enqueue(object : Callback<PrecoBase> {
            override fun onResponse(call: Call<PrecoBase>, response: Response<PrecoBase>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        precoBase(onComplete)
                    }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<PrecoBase>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

    fun finalizar(finalizarViagem: FinalizarViagem, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.finalizar(finalizarViagem)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        finalizar(finalizarViagem, onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun tempoMotoristaParada(posicaoViagemRequest: PosicaoViagemRequest, onComplete: (statusCode: Int, response: ParadaEmbarque?) -> Unit) {
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.tempoMotoristaParada(posicaoViagemRequest)
        call.enqueue(object : Callback<ParadaEmbarque> {
            override fun onResponse(call: Call<ParadaEmbarque>, response: Response<ParadaEmbarque>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        tempoMotoristaParada(posicaoViagemRequest, onComplete)
                    }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<ParadaEmbarque>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

    fun confirmarViagem(confirmarViagem: ConfirmarViagem, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.confirmarViagem(confirmarViagem)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        confirmarViagem(confirmarViagem, onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun cancelarViagem(onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        val viagem = ViagemBo(context).current()
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.cancelarViagem(viagem?.viagem ?: 0, super.id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        cancelarViagem(onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun rotaPassageiroAtivo(passageiroId: Int, onBefore: () -> Unit, onComplete: (statusCode: Int, rotaAtiva: RotaAtiva?) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(RotaPassageiroInterface::class.java)
        val call = service.rotaPassageiroAtiva(passageiroId)
        call.enqueue(object : Callback<RotaAtiva> {
            override fun onResponse(call: Call<RotaAtiva>, response: Response<RotaAtiva>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        rotaPassageiroAtivo(passageiroId, onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<RotaAtiva>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }
}