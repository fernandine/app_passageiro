package mobi.audax.tupi.passageiro.bin.bo

import android.content.Context
import mobi.audax.tupi.passageiro.bin.bean.Passageiro
import mobi.stos.podataka_lib.service.AbstractService

class PassageiroBo(val context: Context)  {


    fun autenticado(): Passageiro? {
        PassengerBo(context).autenticado()?.let {
            val entity = Passageiro()
            entity.id = it.id
            entity.pessoaId = it.pessoa?.id ?: 0
            entity.senha = it.senha
            entity.token = it.token
            it.fireBaseId?.let {
                entity.isGoogle = true
            }
            entity.email = it.email
            entity.nome = it.pessoa?.nome
            entity.cpf = it.pessoa?.cpf
            entity.hash = it.hash
            return entity
        }
        return null
    }

    fun update(passageiro: Passageiro) {
        PassengerBo(context).autenticado()?.let {
            it.hash = passageiro.hash
            it.token = passageiro.token
            this.update(passageiro)
        }
    }
}