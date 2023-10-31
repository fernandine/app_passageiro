package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Arquivo
import mobi.audax.tupi.passageiro.bin.dao.ArquivoDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class ArquivoBo(val context: Context) : AbstractService<Arquivo>() {

    private val dao by lazy { ArquivoDao(context) }

    override fun getDao(): IOperations<Arquivo> {
        return dao
    }
}