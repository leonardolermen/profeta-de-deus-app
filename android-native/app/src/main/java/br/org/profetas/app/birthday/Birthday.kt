package br.org.profetas.app.birthday

data class Birthday(
    val id: String,
    val name: String,
    val day: Int,
    val month: Int,
    val consent: Boolean,
)

data class BirthdayDraft(
    val name: String,
    val day: Int,
    val month: Int,
    val consent: Boolean,
)
