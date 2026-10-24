package br.org.profetas.app.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CredentialsTest {

    @Test fun emailIsTrimmedAndValidated() {
        assertEquals("a@b.co", Email.of(" a@b.co ").value)

        val error = assertThrows(IllegalArgumentException::class.java) { Email.of("a@") }

        assertEquals("Informe um e-mail válido.", error.message)
    }

    @Test fun passwordNeedsEight() {
        assertEquals("12345678", NewPassword.of("12345678").value)

        val error = assertThrows(IllegalArgumentException::class.java) { NewPassword.of("1234567") }

        assertEquals("Use pelo menos 8 caracteres.", error.message)
    }

    @Test fun nameNeedsTwoLetters() {
        assertEquals("Jo", PersonName.of("  Jo ").value)

        val tooShort = assertThrows(IllegalArgumentException::class.java) { PersonName.of(" J ") }
        val tooLong = assertThrows(IllegalArgumentException::class.java) { PersonName.of("a".repeat(121)) }

        assertEquals("Informe seu nome completo.", tooShort.message)
        assertEquals("Informe seu nome completo.", tooLong.message)
    }
}
