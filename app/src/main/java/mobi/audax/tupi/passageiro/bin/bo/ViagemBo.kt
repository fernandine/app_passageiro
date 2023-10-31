package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Viagem
import mobi.audax.tupi.passageiro.bin.dao.ViagemDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class ViagemBo(val context: Context) : AbstractService<Viagem>() {

    private val dao by lazy { ViagemDao(context) }

    override fun getDao(): IOperations<Viagem> {
        return dao
    }

    override fun insert(entity: Viagem?): Long {
        super.clean()
        return super.insert(entity)
    }

    fun current() : Viagem? {
        return dao.get(null, null);
    }

}