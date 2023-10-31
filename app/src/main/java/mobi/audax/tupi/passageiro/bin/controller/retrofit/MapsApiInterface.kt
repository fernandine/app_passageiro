package mobi.audax.tupi.passageiro.bin.controller.retrofit

import mobi.audax.tupi.passageiro.bin.dto.AutoSuggest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApiInterface {

    // result_types=place&Accept-Language=pt-BR&app_id=QNglqZigggLf5NrA4T6B&app_code=eiNoqJT8DvlVYGju_rhmmg
    @GET("autosuggest?result_types=place,address&Accept-Language=pt-BR&app_id=QNglqZigggLf5NrA4T6B&app_code=eiNoqJT8DvlVYGju_rhmmg")
    fun autoSuggest(@Query("at") latLng: String, @Query("q") query : String) : Call<AutoSuggest?>

}