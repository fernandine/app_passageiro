package mobi.audax.tupi.passageiro.bin.dao

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Passenger
import mobi.stos.podataka_lib.repository.AbstractRepository

class PassengerDao(val context: Context) : AbstractRepository<Passenger>(context, Passenger::class.java)