package com.aitolian.sesyazibench.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitolian.sesyazibench.CONSENT_CANCEL
import com.aitolian.sesyazibench.CONSENT_CLOUD
import com.aitolian.sesyazibench.CONSENT_LOCAL
import com.aitolian.sesyazibench.MainViewModel

const val PRIVACY_POLICY_URL = "https://mrzekai.github.io/muteread-privacy.html"

/**
 * İlk bulut aktarımından ÖNCE açık bilgilendirme ve seçim. Seçim yapılmadan ses
 * gönderilmez; geri tuşu / dışarı dokunma "Vazgeç" sayılır (kabul DEĞİL).
 * Reklam izni (UMP) bu seçimin yerine geçmez.
 */
@Composable
fun CloudConsentDialog(vm: MainViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    if (!s.consentAsk) return
    val context = LocalContext.current
    val localMb = vm.localModelDownloadMb()
    Dialog(
        onDismissRequest = { vm.answerCloudConsent(CONSENT_CANCEL) },
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SY.Sheet)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Ses nasıl işlensin?", color = SY.Text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Hızlı yazıya dökme için seçtiğin ses Meta'nın Wit.ai hizmetine gönderilir. Meta sesi kendi " +
                    "koşulları kapsamında işler (en fazla 90 gün saklanır, tanımayı geliştirmek için kullanılabilir). " +
                    "Sesi göndermeden işlemek için \"Telefonda işle\"yi seçebilirsin.",
                color = SY.Text, fontSize = 14.5.sp, lineHeight = 21.sp,
            )
            Text(
                if (localMb == null) "Telefonda işleme hazır; internet gerekmez, uzun seslerde daha yavaştır."
                else "Telefonda işlemek için bir kez ~$localMb MB model indirilir (Wi‑Fi önerilir); uzun seslerde daha yavaştır.",
                color = SY.Muted, fontSize = 13.sp, lineHeight = 18.sp,
            )
            Text(
                "Gizlilik politikası",
                color = SY.Accent, fontSize = 13.5.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))) }
                    }
                    .padding(vertical = 8.dp),
            )
            Text(
                "Seçimin kaydedilir; Ayarlar > Transkript'ten değiştirebilirsin.",
                color = SY.Muted, fontSize = 12.sp,
            )
            ChoiceButton("İnternetle devam et", filled = true) { vm.answerCloudConsent(CONSENT_CLOUD) }
            ChoiceButton("Telefonda işle", filled = false) { vm.answerCloudConsent(CONSENT_LOCAL) }
            ChoiceButton("Vazgeç", filled = false, subtle = true) { vm.answerCloudConsent(CONSENT_CANCEL) }
        }
    }
}

@Composable
private fun ChoiceButton(label: String, filled: Boolean, subtle: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(CircleShape)
            .background(if (filled) SY.Accent else if (subtle) SY.Sheet else SY.Card)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            color = if (filled) SY.OnAccent else if (subtle) SY.Muted else SY.Text,
        )
    }
}
