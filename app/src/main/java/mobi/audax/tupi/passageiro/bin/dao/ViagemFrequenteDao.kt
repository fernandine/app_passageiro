package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.ViagemFrequente
import mobi.stos.podataka_lib.repository.AbstractRepository

class ViagemFrequenteDao(val context: Context) : AbstractRepository<ViagemFrequente>(context, ViagemFrequente::class.java)