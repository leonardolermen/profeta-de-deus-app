package br.org.profetas.app.admin

import br.org.profetas.app.navigation.Route

// As seções da Administração, na ordem dos chips. `newRecord` é o editor do "Novo cadastro"; null nas seções em
// que o admin não cria nada (a igreja já existe; pedidos e inscrições vêm dos membros).
enum class AdminSection(val label: String, val newRecord: Route?) {
    EVENTS("Agenda", Route.EventEditor(Route.NEW_RECORD)),
    WEEKLY_SERVICES("Cultos", Route.WeeklyServiceEditor(Route.NEW_RECORD)),
    STUDIES("Estudos", Route.StudyEditor(Route.NEW_RECORD)),
    PHOTOS("Fotos", Route.PhotoEditor(Route.NEW_RECORD)),
    BIRTHDAYS("Aniversários", Route.BirthdayEditor(Route.NEW_RECORD)),
    CHURCH("Igreja", null),
    PRAYERS("Orações", null),
    REGISTRATIONS("Inscrições", null),
}
