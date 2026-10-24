package br.org.profetas.app.study

data class Study(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val content: String,
)

data class StudyDraft(
    val title: String,
    val description: String,
    val category: String,
    val content: String,
)
