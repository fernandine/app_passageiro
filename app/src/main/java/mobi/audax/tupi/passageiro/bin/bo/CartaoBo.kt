package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Cartao
import mobi.audax.tupi.passageiro.bin.dao.CartaoDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class CartaoBo(val context: Context) : AbstractService<Cartao>() {

    private val dao by lazy { CartaoDao(context) }

    override fun getDao(): IOperations<Cartao> {
        return dao
    }

}