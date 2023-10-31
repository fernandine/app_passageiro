package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Arquivo
import mobi.stos.podataka_lib.repository.AbstractRepository

class ArquivoDao(val context: Context) : AbstractRepository<Arquivo>(context, Arquivo::class.java)