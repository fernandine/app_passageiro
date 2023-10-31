package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.ViagemFrequente
import mobi.audax.tupi.passageiro.bin.dao.ViagemFrequenteDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class ViagemFrequenteBo(val context: Context) : AbstractService<ViagemFrequente>() {

    private val dao by lazy { ViagemFrequenteDao(context) }

    override fun getDao(): IOperations<ViagemFrequente> {
        return dao
    }

    override fun insert(entity: ViagemFrequente?): Long {
        this.clean()
        return super.insert(entity)
    }
}