package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.MinhaRota
import mobi.audax.tupi.passageiro.bin.dao.MinhaRotaDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class MinhaRotaBo(val context: Context) : AbstractService<MinhaRota>() {

    private val dao by lazy { MinhaRotaDao(context) }

    override fun getDao(): IOperations<MinhaRota> {
        return dao
    }

}