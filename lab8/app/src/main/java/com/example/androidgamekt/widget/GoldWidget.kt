package com.example.androidgamekt.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.androidgamekt.R
import com.example.androidgamekt.data.GoldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GoldWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH, AppWidgetManager.ACTION_APPWIDGET_UPDATE -> updateAll(context)
        }
    }

    private fun updateAll(context: Context, manager: AppWidgetManager? = null, ids: IntArray? = null) {
        val appContext = context.applicationContext
        val appWidgetManager = manager ?: AppWidgetManager.getInstance(appContext)
        val appWidgetIds = ids ?: appWidgetManager.getAppWidgetIds(ComponentName(appContext, GoldWidget::class.java))
        if (appWidgetIds.isEmpty()) return

        val loadingText = appContext.getString(R.string.gold_widget_loading)
        updateWidgets(appContext, appWidgetManager, appWidgetIds, loadingText)

        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            val repo = GoldRepository(appContext)
            val fresh = repo.refreshGoldPrice()
            val price = fresh ?: repo.getCachedGoldPrice()
            val text = if (price != null) {
                appContext.getString(R.string.gold_widget_price_template, price)
            } else {
                appContext.getString(R.string.gold_widget_placeholder)
            }
            updateWidgets(appContext, appWidgetManager, appWidgetIds, text)
        }
    }

    private fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray, text: String) {
        ids.forEach { id ->
            manager.updateAppWidget(id, buildRemoteViews(context, text, id))
        }
    }

    private fun buildRemoteViews(context: Context, text: String, appWidgetId: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_gold_ball).apply {
            setTextViewText(R.id.tvGoldPrice, text)
            setOnClickPendingIntent(R.id.widgetRoot, buildRefreshPendingIntent(context, appWidgetId))
        }
    }

    private fun buildRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, GoldWidget::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_REFRESH = "com.example.androidgamekt.widget.ACTION_REFRESH"
    }
}

