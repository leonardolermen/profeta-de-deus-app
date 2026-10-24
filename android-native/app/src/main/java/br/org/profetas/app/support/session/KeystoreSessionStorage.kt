package br.org.profetas.app.support.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Tokens criptografados com chave não exportável do Android Keystore. Backup desativado. */
class KeystoreSessionStorage(context: Context) : SessionStorage {

    private val preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey("profetas_session", null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    "profetas_session",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }

    override fun save(session: Session) {
        val json = JSONObject()
            .put("access", session.access)
            .put("refresh", session.refresh)
            .put("id", session.userId)
            .put("email", session.email)
            .toString()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(json.toByteArray(Charsets.UTF_8))

        preferences.edit()
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("data", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    override fun load(): Session? = runCatching {
        val data = preferences.getString("data", null) ?: return null
        val iv = Base64.decode(preferences.getString("iv", ""), Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        }
        val decoded = String(cipher.doFinal(Base64.decode(data, Base64.NO_WRAP)), Charsets.UTF_8)
        val user = JSONObject(decoded)
        Session(user.getString("access"), user.getString("refresh"), user.getString("id"), user.getString("email"))
    }.getOrElse {
        preferences.edit().clear().apply()
        null
    }
}
