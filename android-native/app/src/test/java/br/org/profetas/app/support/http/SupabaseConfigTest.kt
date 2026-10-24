package br.org.profetas.app.support.http

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseConfigTest {

    @Test fun releaseRejectsLocalHttp() {
        assertFalse(SupabaseConfig("http://10.0.2.2:54321", "k", allowLocalHttp = false).isUsable)
    }

    @Test fun debugAcceptsEmulatorHost() {
        assertTrue(SupabaseConfig("http://10.0.2.2:54321", "k", allowLocalHttp = true).isUsable)
    }

    @Test fun debugStillRejectsArbitraryHttp() {
        assertFalse(SupabaseConfig("http://example.org", "k", allowLocalHttp = true).isUsable)
    }

    @Test fun httpsAlwaysAccepted() {
        assertTrue(SupabaseConfig("https://abc.supabase.co", "k", allowLocalHttp = false).isUsable)
    }
}
