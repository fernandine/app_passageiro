package mobi.audax.tupi.passageiro.bin.dto

import com.google.gson.annotations.Expose

class AutoSuggest {

    @Expose
    var results: List<AutoSuggestResult> = arrayListOf()

    inner class AutoSuggestResult {

        @Expose
        var id = ""

        @Expose
        var title = ""

        @Expose
        var vicinity = ""

        @Expose
        var position: DoubleArray = DoubleArray(0)

        @Expose
        var categoryTitle = ""

        @Expose
        var distance = 0
    }

}
