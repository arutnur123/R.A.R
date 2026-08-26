package com.example.data.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class WhatsAppTarget(
    val id: String,
    val name: String,
    val phoneNumber: String = "", // with country code without plus e.g. 905551234567
    val isGroup: Boolean = false,
    val avatarEmoji: String = "👤",
    val recentMessage: String = "Mesaj bekleniyor..."
)

data class QuickReplyTemplate(
    val id: String,
    val title: String,
    val templateText: String,
    val category: String = "Genel" // Hızlı, İş, Acil, Toplantı, Kibar
)

object WhatsAppDefaults {
    val defaultTargets = listOf(
        WhatsAppTarget(
            id = "t1",
            name = "Proje Ekibi (Grup)",
            phoneNumber = "",
            isGroup = true,
            avatarEmoji = "👥",
            recentMessage = "Sunum dosyaları hazır mı?"
        ),
        WhatsAppTarget(
            id = "t2",
            name = "Ahmet Yılmaz",
            phoneNumber = "905551234567",
            isGroup = false,
            avatarEmoji = "👨‍💻",
            recentMessage = "Yarın saat 10'da görüşelim mi?"
        ),
        WhatsAppTarget(
            id = "t3",
            name = "Çalışma & Sınav Grubu",
            phoneNumber = "",
            isGroup = true,
            avatarEmoji = "📚",
            recentMessage = "Hangi konulardan sorumluyuz?"
        ),
        WhatsAppTarget(
            id = "t4",
            name = "Zeynep Kaya",
            phoneNumber = "905559876543",
            isGroup = false,
            avatarEmoji = "👩‍💼",
            recentMessage = "Kod taslağını inceledin mi?"
        ),
        WhatsAppTarget(
            id = "t5",
            name = "Aile Grubu",
            phoneNumber = "",
            isGroup = true,
            avatarEmoji = "🏡",
            recentMessage = "Akşam kaçta geliyorsun?"
        )
    )

    val defaultTemplates = listOf(
        QuickReplyTemplate("q1", "Toplantıdayım", "Şu an toplantıdayım/odak modundayım. En kısa sürede geri dönüş yapacağım.", "İş"),
        QuickReplyTemplate("q2", "R.A.R Onayı", "R.A.R Asistanım üzerinden onaylandı. Teşekkürler!", "Hızlı"),
        QuickReplyTemplate("q3", "Üzerinde Çalışıyorum", "Mesajını aldım, ilgili konu üzerinde çalışıyorum.", "İş"),
        QuickReplyTemplate("q4", "Müsait Olunca Arayacağım", "Şu an konuşamıyorum, birazdan arayacağım.", "Acil"),
        QuickReplyTemplate("q5", "Harika / Tamamdır", "Harika, tamamdır! Anlaştık.", "Kibar")
    )
}

class WhatsAppManager(private val context: Context) {

    fun sendWhatsAppMessage(target: WhatsAppTarget?, customPhone: String, messageText: String): Boolean {
        val phone = when {
            customPhone.isNotBlank() -> cleanPhoneNumber(customPhone)
            target != null && target.phoneNumber.isNotBlank() -> cleanPhoneNumber(target.phoneNumber)
            else -> ""
        }

        val encodedText = try {
            URLEncoder.encode(messageText, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            messageText
        }

        return try {
            val intent = if (phone.isNotBlank()) {
                // Direct specific phone chat
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedText")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                // Open WhatsApp chooser / group share
                val uri = Uri.parse("whatsapp://send?text=$encodedText")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }

            // Verify if WhatsApp is installed, otherwise fallback to web
            try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                // Fallback to web browser WhatsApp link
                val fallbackUri = if (phone.isNotBlank()) {
                    Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedText")
                } else {
                    Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun cleanPhoneNumber(phone: String): String {
        return phone.replace("+", "")
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()
    }
}
