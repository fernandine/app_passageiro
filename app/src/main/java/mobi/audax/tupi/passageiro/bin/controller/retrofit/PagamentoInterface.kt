package mobi.audax.tupi.passageiro.bin.controller.retrofit

import mobi.audax.tupi.passageiro.bin.dto.CartaoDto
import mobi.audax.tupi.passageiro.bin.dto.ListaCartaoDto
import mobi.audax.tupi.passageiro.bin.dto.Pix
import mobi.audax.tupi.passageiro.bin.dto.PixDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PagamentoInterface {

    @POST("pagamento/cartao/passageiro/{hash}")
    fun createCartao(@Path("hash") hash: String, @Body cartao: CartaoDto): Call<ResponseBody>

    @GET("pagamento/cartao/listarCartoes/{hash}")
    fun listCartao(@Path("hash") hash: String): Call<List<ListaCartaoDto>>

    @DELETE("pagamento/cartao/{id}")
    fun delete(@Path("id") id: Int): Call<ResponseBody>

    @POST("pagamento/pix")
    fun createPix(@Body pix: Pix): Call<PixDto>

}
