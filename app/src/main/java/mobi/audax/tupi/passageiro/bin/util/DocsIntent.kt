package mobi.audax.tupi.passageiro.bin.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import mobi.audax.tupi.passageiro.R

class DocsIntent {

    companion object {

        @JvmStatic
        fun termos(context: Context) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(context.getString(R.string.url_termos))
            context.startActivity(i)
        }

        @JvmStatic
        fun privacidade(context: Context) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(context.getString(R.string.url_privacidade))
            context.startActivity(i)
        }


    }
}