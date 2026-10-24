package br.org.profetas.app.support.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageResizeTest {

    @Test fun landscapeLimitsWidth() {
        assertEquals(TargetSize(1600, 1200), targetSize(4000, 3000))
    }

    @Test fun portraitLimitsHeight() {
        assertEquals(TargetSize(1200, 1600), targetSize(3000, 4000))
    }

    @Test fun smallImageIsNotEnlarged() {
        assertEquals(TargetSize(800, 600), targetSize(800, 600))
    }

    @Test fun sampleSizeKeepsAtLeastMaxSide() {
        assertEquals(2, sampleSizeFor(6000, 4000, 1600))
        assertEquals(1, sampleSizeFor(1000, 800, 1600))
    }
}
