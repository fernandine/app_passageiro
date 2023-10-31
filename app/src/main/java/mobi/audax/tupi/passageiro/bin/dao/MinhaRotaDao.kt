package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.MinhaRota
import mobi.stos.podataka_lib.repository.AbstractRepository

class MinhaRotaDao(val context: Context) : AbstractRepository<MinhaRota>(context, MinhaRota::class.java)