package br.org.profetas.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RouteTest {

    @Test fun parsesDebugLinks() {
        assertEquals(Route.Events, routeFromDebugLink("profetas://open/events"))
        assertEquals(Route.EventDetail("abc"), routeFromDebugLink("profetas://open/event/abc"))
        assertNull(routeFromDebugLink("https://open/events"))
    }

    @Test fun tabsAreTheFourFromExpo() {
        assertEquals(listOf(Route.Home, Route.Bible, Route.Events, Route.Settings), Route.tabs)
    }
}
