package br.org.profetas.app.event

import br.org.profetas.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class EventTypeTest {

    @Test fun knownTypesMapToTheirImages() {
        assertEquals(EventType.BATISMO, EventType.fromDatabase("batismo"))
        assertEquals(R.drawable.batismo, EventType.BATISMO.defaultImage)

        assertEquals(EventType.JOVENS, EventType.fromDatabase("jovens"))
        assertEquals(R.drawable.jovens, EventType.JOVENS.defaultImage)

        assertEquals(EventType.MULHERES, EventType.fromDatabase("mulheres"))
        assertEquals(R.drawable.culto_mulheres, EventType.MULHERES.defaultImage)
    }

    @Test fun unknownTypeFallsBackToCulto() {
        assertEquals(EventType.CULTO, EventType.fromDatabase("retiro"))
        assertEquals(EventType.CULTO, EventType.fromDatabase("Culto"))
        assertEquals(EventType.CULTO, EventType.fromDatabase(""))
    }
}
