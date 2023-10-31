package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Pessoa
import mobi.audax.tupi.passageiro.bin.dao.PessoaDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class PessoaBo(val context: Context) : AbstractService<Pessoa>() {

    private val dao by lazy { PessoaDao(context) }

    override fun getDao(): IOperations<Pessoa> {
        return dao
    }
}