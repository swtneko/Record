package com.neko.record

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt component graph roots here.
 *
 * Later milestones attach process-wide setup here too (WorkManager
 * configuration for the auto-restart service, crash reporting init, etc.)
 * — kept minimal for Milestone 1 on purpose.
 */
@HiltAndroidApp
class NekoRecordApp : Application()
