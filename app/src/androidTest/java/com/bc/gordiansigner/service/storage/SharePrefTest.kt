package com.bc.gordiansigner.service.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import com.bc.gordiansigner.service.storage.sharedpref.rxCompletable
import com.bc.gordiansigner.service.storage.sharedpref.rxSingle
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharePrefTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testReadWriteStandardSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).STANDARD.rxCompletable { pref -> pref.put(d.key, d.value) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).STANDARD.rxSingle { pref -> pref.get(d.key, String::class) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(d.value)
        }
    }

    @Test
    fun testReadWriteSecureSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).SECURE.rxCompletable { pref ->
                pref.put(d.key, d.value)
            }.test().assertComplete().assertNoErrors()

            SharedPrefApi(appContext).SECURE.rxSingle { pref ->
                pref.get(d.key, String::class)
            }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(d.value)

        }
    }

    @Test
    fun testDeleteStandardSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).STANDARD.rxCompletable { pref -> pref.put(d.key, d.value) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).STANDARD.rxCompletable { pref -> pref.clear(d.key) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).STANDARD.rxSingle { pref -> pref.get(d.key, String::class) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue("")

        }
    }

    @Test
    fun testDeleteSecureSharedPref() {
        val dataSet =
            mapOf("key1" to "val", "key2" to " ", "key3" to "valvalvalvalvalvalvalvalvalvalvalval")

        for (d in dataSet.entries) {
            SharedPrefApi(appContext).SECURE.rxCompletable { pref -> pref.put(d.key, d.value) }
                .test()
                .assertComplete()
                .assertNoErrors()


            SharedPrefApi(appContext).SECURE.rxCompletable { pref -> pref.clear(d.key) }
                .test()
                .assertComplete()
                .assertNoErrors()

            SharedPrefApi(appContext).SECURE.rxSingle { pref -> pref.get(d.key, String::class) }
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue("")

        }
    }

}