package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Baller Path", appName)
  }

  @Test
  fun `phase A package id and backup disabled are configured`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("com.ballerpath.app", BuildConfig.APPLICATION_ID)
    assertEquals("com.ballerpath.app", context.packageName)
    val allowBackup = (context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
    assertFalse(allowBackup)
  }
}
