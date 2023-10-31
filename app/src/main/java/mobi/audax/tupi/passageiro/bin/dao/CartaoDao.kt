package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Cartao
import mobi.stos.podataka_lib.repository.AbstractRepository

class CartaoDao(val context: Context) : AbstractRepository<Cartao>(context, Cartao::class.java)