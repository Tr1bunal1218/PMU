package com.example.androidgamekt.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GoldRepository(private val context: Context) {
    private val api by lazy { GoldApi.create() }

    private val cbrTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Novosibirsk")

    suspend fun refreshGoldPrice(): Float? = withContext(Dispatchers.IO) {
        return@withContext try {
            val locale = Locale.forLanguageTag("ru-RU")
            val rangeFormatter = SimpleDateFormat("dd/MM/yyyy", locale).apply {
                timeZone = cbrTimeZone
            }
            val parserDateFormatter = SimpleDateFormat("dd.MM.yyyy", locale).apply {
                timeZone = cbrTimeZone
            }

            val toCalendar = Calendar.getInstance(cbrTimeZone)
            val fromCalendar = (toCalendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -14)
            }
            val todayString = parserDateFormatter.format(toCalendar.time)

            val xml = api.getMetalsXml(
                rangeFormatter.format(fromCalendar.time),
                rangeFormatter.format(toCalendar.time)
            )

            val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
            val parser = factory.newPullParser().apply {
                setInput(StringReader(xml))
            }

            var latestPrice: Float? = null
            var latestDate: Date? = null
            var isGoldRecord = false
            var isTodayRecord = false
            var recordDate: Date? = null
            var todayPrice: Float? = null

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Record" -> {
                                val code = parser.getAttributeValue(null, "Code")
                                if (code == "1") { // 1 == Gold
                                    isGoldRecord = true
                                    val dateAttr = parser.getAttributeValue(null, "Date")
                                    isTodayRecord = dateAttr == todayString
                                    recordDate = try {
                                        dateAttr?.let { parserDateFormatter.parse(it) }
                                    } catch (_: ParseException) {
                                        null
                                    }
                                } else {
                                    isGoldRecord = false
                                    isTodayRecord = false
                                    recordDate = null
                                }
                            }

                            "Buy" -> if (isGoldRecord) {
                                val value = parser.nextText().trim().replace(',', '.').toFloatOrNull()
                                if (value != null) {
                                    val date = recordDate
                                    if (date != null) {
                                        if (isTodayRecord) {
                                            todayPrice = value
                                        }
                                        if (latestDate == null || date.after(latestDate)) {
                                            latestDate = date
                                            latestPrice = value
                                        }
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> if (parser.name == "Record") {
                        isGoldRecord = false
                        isTodayRecord = false
                        recordDate = null
                    }
                }
                event = parser.next()
            }

            val finalPrice = todayPrice ?: latestPrice
            finalPrice?.takeIf { it > 0f }?.also {
                Prefs(context).setGoldPriceRubPerGram(it)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getCachedGoldPrice(): Float? = Prefs(context).getGoldPriceRubPerGram()
}
