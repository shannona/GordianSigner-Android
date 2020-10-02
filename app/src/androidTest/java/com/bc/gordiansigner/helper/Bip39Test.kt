package com.bc.gordiansigner.helper

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blockstream.libwally.Wally.base58_from_bytes
import com.blockstream.libwally.Wally.hex_to_bytes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Bip39Test {

    @Test
    fun testMnemonicFromBytes() {
        val dataSet = listOf(
            Pair(
                hex_to_bytes("e6981fd5982fa80789dd325a8e79b9df"),
                "track science voice core whisper adult cherry play fold inherit hover sand"
            ),
            Pair(
                hex_to_bytes("85d834ded1105147fd75e739e46cccd9"),
                "magic script dash peasant agree physical volume rude delay casino creek ready"
            )
        )

        for (data in dataSet) {
            val actual = Bip39.mnemonicFromBytes(data.first)
            assertEquals(data.second, actual)
        }
    }

    @Test
    fun testSeedFromMnemonic() {
        val dataSet = listOf(
            Pair(
                "track science voice core whisper adult cherry play fold inherit hover sand",
                "z15N6NmpJSjZvfAPRtt4gc8QQShgeQqdjauVnowFU8pQSG7iwpdCK51V3p5jBe6qMKgggJfnS9HAu8KNvKNQKr1"
            ),
            Pair(
                "magic script dash peasant agree physical volume rude delay casino creek ready",
                "hzbKBx624YEJoDUYGDQbwtzAsTQGYsQh6Le5qsGkM1XypS6S6fT21y6AZaW2MGQ9zDETZTEHBb1Gv1qEVsqNf6N"
            )
        )

        for (data in dataSet) {
            val actual = Bip39.seedFromMnemonic(data.first)
            assertEquals(data.second, base58_from_bytes(actual, 0))
        }
    }
}