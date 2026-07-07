package com.neko.record

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test confirming the debug application context resolves with the
 * expected base package. Expand with Compose UI tests (e.g. asserting the six
 * platform tiles render) once the Espresso/Compose test harness is wired up
 * in a later milestone.
 */
@RunWith(AndroidJUnit4::class)
class ApplicationIdTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.neko.record.debug", appContext.packageName)
    }
}
