package mobi.audax.tupi.passageiro.bin.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import mobi.audax.tupi.passageiro.R
import mobi.audax.tupi.passageiro.activities.home.novodestino.NavigationActivity
import mobi.audax.tupi.passageiro.activities.splash.SplashActivity
import org.apache.commons.lang3.StringUtils
import java.io.IOException

class SNotification(val context: Context) {

    private val NOTIFICATION_ID = 1
    private val NOTIFICATION_CHANNEL_ID = "Central de Notificações"
    private val GROUP_KEY_NOTIFICATION = "mobi.audax.tupi.bin.util.SNotification"

    private var notificationBuilder: NotificationCompat.Builder? = null

    private fun setChannel(notificationManager: NotificationManager) {
        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Mensagens", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = "Central de Notificações"
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.vibrationPattern = longArrayOf(0, 500, 500, 500)
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun imageNotification(title: String, body: String, image: String?) {
        val bigPicStyle = NotificationCompat.BigPictureStyle()
        if (StringUtils.isNotBlank(image)) {
            bigPicStyle.bigPicture(getBitmapFromURL(image!!))
        }
        bigPicStyle.setBigContentTitle(title)
        bigPicStyle.setSummaryText(body)
        notificationBuilder!!.setStyle(bigPicStyle)
    }

    fun sendNotification(title: String, body: String, image: String?, action: String?, badge: Int) {
        notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        notificationBuilder!!.setDefaults(Notification.DEFAULT_ALL)
        notificationBuilder!!.color = ContextCompat.getColor(context, R.color.primary)
        notificationBuilder!!.setAutoCancel(true)
        notificationBuilder!!.setSmallIcon(R.drawable.ic_stat)
        notificationBuilder!!.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_icon))
        notificationBuilder!!.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        notificationBuilder!!.setContentTitle(title)
        notificationBuilder!!.setContentText(body)
        notificationBuilder!!.setGroup(GROUP_KEY_NOTIFICATION)

        if (StringUtils.isNotBlank(image)) {
            imageNotification(title, body, image)
        }
        if (StringUtils.isBlank(action)) {
            val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, SplashActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder!!.setContentIntent(pendingIntent)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setChannel(notificationManager)
        notificationManager.notify(0, notificationBuilder!!.build())
    }

    fun sendNotification(title: String?, messages: List<String?>, pendingIntent: PendingIntent?, resSmallIcon: Int) {
        notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        notificationBuilder!!.setSmallIcon(resSmallIcon)
        notificationBuilder!!.setWhen(System.currentTimeMillis())
        notificationBuilder!!.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_icon))
        notificationBuilder!!.setAutoCancel(true)
        notificationBuilder!!.setDefaults(Notification.DEFAULT_ALL) // exibe notificação somente se não estiver no silencioso.
        if (title != null) {
            notificationBuilder!!.setContentTitle(title)
        }
        val inbox = NotificationCompat.InboxStyle()
        inbox.setBigContentTitle(context.getString(R.string.mensagens))
        var lastMessage: String? = ""
        for (message in messages) {
            inbox.addLine(message)
            lastMessage = message
        }
        notificationBuilder!!.setContentText(lastMessage)
        notificationBuilder!!.setStyle(inbox)
        notificationBuilder!!.setContentIntent(pendingIntent)
        notificationBuilder!!.setGroup(GROUP_KEY_NOTIFICATION)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setChannel(notificationManager)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder!!.build())
    }

    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            Picasso.get().load(strURL).get()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


}