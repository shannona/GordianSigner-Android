package com.bc.gordiansigner.helper

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blockstream.libwally.Wally.base58_from_bytes
import com.blockstream.libwally.Wally.base58_to_bytes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Bip32Test {

    @Test
    fun testRandomEntropy() {
        // TODO add later
    }

    @Test
    fun testGenerateKeyPair() {
        val dataSet = listOf(
            Triple(
                "z15N6NmpJSjZvfAPRtt4gc8QQShgeQqdjauVnowFU8pQSG7iwpdCK51V3p5jBe6qMKgggJfnS9HAu8KNvKNQKr1",
                "tprv8ZgxMBicQKsPdMSLMWwBoHNYaCqjZMSFfmHcASHWJurVS3jKms6UMN45G78fz9HRfzTK5Q3DJJNsaZaKGkheDEbynBdKdzFujsjUwDKEbvK",
                "tpubD6NzVbkrYhZ4WpU8FAbnCh2f9EMfigdAF4tPSxKojBetGXz6QFv4XrfwSD7gYQiNFLKVEENunaiWUZc7jVqr7oKqDcqw9UtRtGsHGhK2iya"
            ),
            Triple(
                "hzbKBx624YEJoDUYGDQbwtzAsTQGYsQh6Le5qsGkM1XypS6S6fT21y6AZaW2MGQ9zDETZTEHBb1Gv1qEVsqNf6N",
                "tprv8ZgxMBicQKsPesYBono69mqvmRZfHZPgxXQ2PyPao9vm1ZA1EdcNocu8heex3X3jegjbJ77PuNp6GXY2nSZpKjfSVsCB31aukCEAy4jZFhJ",
                "tpubD6NzVbkrYhZ4YLZyhSTgZBW3LT5bStabXpzogVRtDRj9r3Qms2Rxz7WzsoBoSJRUYZzhKZUwzLfEmq6DheVxCUTxG3sm54WSDM4wgBMu1zy"
            )
        )

        for (data in dataSet) {
            val kp = Bip32.generateKeyPair(Network.TEST, base58_to_bytes(data.first))
            assertEquals(data.second, base58_from_bytes(kp.prv, 0))
            assertEquals(data.third, base58_from_bytes(kp.pub, 0))
        }
    }

}