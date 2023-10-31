package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Passenger
import mobi.audax.tupi.passageiro.bin.dao.PassengerDao
import mobi.stos.podataka_lib.interfaces.IOperations
import mobi.stos.podataka_lib.service.AbstractService

class PassengerBo(val context: Context) : AbstractService<Passenger>() {

    private val dao by lazy { PassengerDao(context) }
    private val arquivoBo by lazy { ArquivoBo(context) }
    private val cartaoBo by lazy { CartaoBo(context) }
    private val pessoaBo by lazy { PessoaBo(context) }

    override fun getDao(): IOperations<Passenger> {
        return dao
    }

    override fun insert(entity: Passenger?): Long {
        this.hardReset()

        arquivoBo.insert(entity?.pessoa?.fotoPerfil)
        cartaoBo.insert(entity?.cartao)
        pessoaBo.insert(entity?.pessoa)
        return super.insert(entity)
    }

    fun autenticado(): Passenger? {
        val entity = dao.get(null, null)
        entity?.let {
            val pessoa = pessoaBo.get("id = ?", arrayOf(it.pessoa?.id.toString()))
            val ark = arquivoBo.get("id = ?", arrayOf(pessoa?.fotoPerfil?.id.toString()))
            pessoa.fotoPerfil = ark

//            val card = cartaoBo.get("id = ?", arrayOf(it.cartao?.id.toString()))
//            it.cartao = card
            it.pessoa = pessoa
        }
        return entity
    }

    /**
     * Limpa todos os dados do passageiro, documentos e cartão de crédito cadastrado.
     */
    private fun hardReset() {
        arquivoBo.clean()
        cartaoBo.clean()
        pessoaBo.clean()
        super.clean()
    }

    fun logout() {
        this.hardReset()
    }

}