package br.org.profetas.app.account

// Regex simples e JVM-only (android.util.Patterns não existe nos testes unitários). O servidor é quem decide se o
// endereço existe; aqui só se barra o que claramente não é um e-mail.
private val EMAIL_FORMAT = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private const val MAX_EMAIL_LENGTH = 254
private const val MIN_PASSWORD_LENGTH = 8
private const val MIN_PERSON_NAME_LENGTH = 2
const val MAX_PERSON_NAME_LENGTH = 120

@JvmInline
value class Email private constructor(val value: String) {

    companion object {

        fun of(raw: String): Email {
            val trimmed = raw.trim()
            val isValid = trimmed.length <= MAX_EMAIL_LENGTH && EMAIL_FORMAT.matches(trimmed)
            require(isValid) { "Informe um e-mail válido." }

            return Email(trimmed)
        }
    }
}

// Só para senha nova (cadastro e troca): a senha de quem entra não é conferida aqui, porque contas antigas podem ter
// regras anteriores e o servidor é quem a aceita ou recusa. Não é aparada: espaço nas pontas faz parte da senha.
@JvmInline
value class NewPassword private constructor(val value: String) {

    companion object {

        fun of(raw: String): NewPassword {
            require(raw.length >= MIN_PASSWORD_LENGTH) { "Use pelo menos 8 caracteres." }

            return NewPassword(raw)
        }
    }
}

@JvmInline
value class PersonName private constructor(val value: String) {

    companion object {

        fun of(raw: String): PersonName {
            val trimmed = raw.trim()
            require(trimmed.length in MIN_PERSON_NAME_LENGTH..MAX_PERSON_NAME_LENGTH) {
                "Informe seu nome completo."
            }

            return PersonName(trimmed)
        }
    }
}
