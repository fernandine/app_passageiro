package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Viagem
import mobi.stos.podataka_lib.repository.AbstractRepository

class ViagemDao(val context: Context) : AbstractRepository<Viagem>(context, Viagem::class.java)