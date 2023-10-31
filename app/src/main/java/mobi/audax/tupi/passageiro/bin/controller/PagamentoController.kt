package mobi.audax.tupi.passageiro.bin.controller

import android.content.Context
import mobi.audax.tupi.passageiro.bin.controller.retrofit.PagamentoInterface
import mobi.audax.tupi.passageiro.bin.dto.CartaoDto
import mobi.audax.tupi.passageiro.bin.dto.ListaCartaoDto
import mobi.audax.tupi.passageiro.bin.dto.Pix
import mobi.audax.tupi.passageiro.bin.dto.PixDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PagamentoController(override val context: Context) : CommonsController(context) {

    fun getListCartao(onBefore: () -> Unit, onComplete: (statusCode: Int, response: List<ListaCartaoDto>?) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PagamentoInterface::class.java)
        val call = service.listCartao(hash)
        call.enqueue(object : Callback<List<ListaCartaoDto>> {
            override fun onResponse(call: Call<List<ListaCartaoDto>>, response: Response<List<ListaCartaoDto>>) {
                if (response.code() == 401) {
                    tryRefreshToken {
                        getListCartao(onBefore, onComplete)
                    }
                } else {
                    onComplete(response.code(), response.body())
                }
            }

            override fun onFailure(call: Call<List<ListaCartaoDto>>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

    fun createCartao(cartaoDTODto: CartaoDto, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PagamentoInterface::class.java)
        val call = service.createCartao(hash, cartaoDTODto)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onComplete(response.code())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun deleteCartao(id: Int, onBefore: () -> Unit, onComplete: (statusCode: Int) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PagamentoInterface::class.java)
        val call = service.delete(id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onComplete(response.code())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(999)
            }
        })
    }

    fun createPix(pix: Pix, onBefore: () -> Unit, onComplete: (statusCode: Int, response: PixDto?) -> Unit) {
        super.onBeforeExecute(onBefore)
        val service = retrofit.create(PagamentoInterface::class.java)
        pix.valor = 50.0
        val call = service.createPix(pix)
        call.enqueue(object : Callback<PixDto> {
            override fun onResponse(call: Call<PixDto>, response: Response<PixDto>) {
                onComplete(response.code(), response.body())
            }

            override fun onFailure(call: Call<PixDto>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

}