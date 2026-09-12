package com.sravila.apexmoney.features.recurring

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PaymentNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val paymentTitle = inputData.getString("PAYMENT_TITLE") ?: "Suscripción"
        val paymentAmount = inputData.getDouble("PAYMENT_AMOUNT", 0.0)

        showNotification(applicationContext, paymentTitle, paymentAmount)
        return Result.success()
    }

    private fun showNotification(context: Context, title: String, amount: Double) {
        val channelId = "recurring_payments_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pagos Recurrentes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
            .setContentTitle("Recordatorio de Pago")
            .setContentText("Hoy es el día de pago para: $title ($$amount)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
