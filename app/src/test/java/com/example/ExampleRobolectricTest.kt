package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PRESET_AUDIENCES
import com.example.data.model.UserSubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CreatorForge AI", appName)
  }

  @Test
  fun `verify preset audiences exist`() {
    assertTrue(PRESET_AUDIENCES.isNotEmpty())
    val youngPros = PRESET_AUDIENCES.find { it.id == "young_pros" }
    assertTrue(youngPros != null)
    assertTrue(youngPros!!.highConvertingCTAs.isNotEmpty())
  }

  @Test
  fun `verify subscription tiers`() {
    assertEquals(50, UserSubscriptionTier.FREE.monthlyCredits)
    assertEquals(1000, UserSubscriptionTier.PRO.monthlyCredits)
    assertEquals(5000, UserSubscriptionTier.AGENCY.monthlyCredits)
  }
}
