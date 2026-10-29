package br.org.profetas.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.BuildConfig
import br.org.profetas.app.account.AccountViewModel
import br.org.profetas.app.account.ContentAccess
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.PrimaryButton
import br.org.profetas.app.support.ui.ScreenColumn

private const val ABOUT_TEXT = "Levando a palavra de Deus e eventos da igreja diretamente para seu dispositivo " +
    "de maneira imersiva e moderna."

// app/(tabs)/settings.tsx. O "Habilitar Notificações" do Expo (um interruptor sem efeito) virou o item de lembretes
// de eventos, que de fato existem no app.
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    accountViewModel: AccountViewModel,
    onNavigate: (Route) -> Unit,
) {
    LaunchedEffect(Unit) {
        settingsViewModel.reload()
    }

    ScreenColumn(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Ajustes",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp),
        )

        AccountSection(accountViewModel, onNavigate)

        PreferencesSection(settingsViewModel)

        AboutSection(onNavigate)
    }
}

@Composable
private fun AccountSection(accountViewModel: AccountViewModel, onNavigate: (Route) -> Unit) {
    val isLoggedIn by accountViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val profile by accountViewModel.profile.collectAsStateWithLifecycle()

    val isAdmin = isLoggedIn && ContentAccess.canManage(profile)
    val displayName = profile?.name?.takeIf { name -> name.isNotBlank() } ?: accountViewModel.email

    // O perfil só existe depois de carregado: sem isso o administrador apareceria como usuário.
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            accountViewModel.loadProfile()
        }
    }

    SettingsHeading("CONTA")

    SettingsGroup {
        SettingsRow(label = if (isLoggedIn) "Logado como $displayName" else "Não conectado") {
            if (isLoggedIn) {
                PrimaryButton(label = "Sair", onClick = accountViewModel::logout)
            } else {
                PrimaryButton(label = "Entrar", onClick = { onNavigate(Route.Login) })
            }
        }

        if (isLoggedIn) {
            SettingsDivider()

            SettingsRow(label = "Modo", detail = if (isAdmin) "Administrador" else "Usuário") {}

            SettingsDivider()

            LinkRow("Meu perfil") { onNavigate(Route.Profile) }

            SettingsDivider()

            LinkRow("Minhas inscrições") { onNavigate(Route.MyRegistrations) }
        }

        if (isAdmin) {
            SettingsDivider()

            LinkRow("Administração") { onNavigate(Route.Admin) }
        }
    }

    if (isAdmin) {
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Painel do administrador", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Crie eventos e gerencie estudos, fotos e informações da igreja.", fontSize = 17.sp)
                Button(onClick = { onNavigate(Route.EventEditor(Route.NEW_RECORD)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text("Criar novo evento", fontSize = 18.sp)
                }
                OutlinedButton(onClick = { onNavigate(Route.Admin) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("Ver todas as funções", fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun PreferencesSection(settingsViewModel: SettingsViewModel) {
    val isDark by settingsViewModel.isDark.collectAsStateWithLifecycle()
    val reminderCount by settingsViewModel.reminderCount.collectAsStateWithLifecycle()

    SettingsHeading("PREFERÊNCIAS")

    SettingsGroup {
        SettingsRow(
            label = "Lembretes de eventos",
            detail = if (reminderCount == 0) "Nenhum lembrete ativo" else "$reminderCount lembrete(s) ativo(s)",
        ) {
            if (reminderCount > 0) {
                PrimaryButton(label = "Desativar todos", onClick = settingsViewModel::cancelAllReminders)
            }
        }

        SettingsDivider()

        SettingsRow(label = "Tema escuro") {
            Switch(
                checked = isDark,
                onCheckedChange = { checked ->
                    if (checked) {
                        settingsViewModel.useDark()
                    } else {
                        settingsViewModel.useLight()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D9E0),
                    uncheckedBorderColor = Color(0xFFD1D9E0),
                ),
            )
        }
    }
}

@Composable
private fun AboutSection(onNavigate: (Route) -> Unit) {
    SettingsHeading("SOBRE O APP")

    SettingsGroup {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(
                text = "Igreja Profetas de Deus",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Text(
                text = "Versão ${BuildConfig.VERSION_NAME} — $ABOUT_TEXT",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "Termos de Serviço e Privacidade",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { onNavigate(Route.Privacy) },
            )

            Text(
                text = "Entre em contato conosco",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onNavigate(Route.Contact) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    SettingsRow(label = label, modifier = Modifier.clickable(onClick = onClick)) {}
}
