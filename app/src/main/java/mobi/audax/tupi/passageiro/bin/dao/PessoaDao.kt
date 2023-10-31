package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Pessoa
import mobi.stos.podataka_lib.repository.AbstractRepository

class PessoaDao(val context: Context) : AbstractRepository<Pessoa>(context, Pessoa::class.java)