package com.bc.ur

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.gordiansigner.helper.Hex
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class URDecoderTest {

    @Test
    fun testDecodeSinglePart() {
        val expectedUR = UR.create(50, "Wolf")
        val encoded =
            "ur:bytes/hdeymejtswhhylkepmykhhtsytsnoyoyaxaedsuttydmmhhpktpmsrjtgwdpfnsboxgwlbaawzuefywkdplrsrjynbvygabwjldapfcsdwkbrkch"
        val ur = URDecoder.decode(encoded)
        assertEquals(expectedUR.type, ur.type)
        assertEquals(Hex.hexFromBytes(expectedUR.cbor), Hex.hexFromBytes(ur.cbor))
    }

    @Test
    fun testDecodeMultiParts() {
        val ur = UR.create(32767, "Wolf")
        val encoder = UREncoder(ur, 1000, 100, 10)
        val decoder = URDecoder()
        do {
            val part = encoder.nextPart()
            decoder.receivePart(part)
        } while (!decoder.isComplete)

        if (decoder.isSuccess) {
            val resultUR = decoder.resultUR()
            assertEquals(ur.type, resultUR.type)
            assertEquals(Hex.hexFromBytes(ur.cbor), Hex.hexFromBytes(resultUR.cbor))
        } else {
            val ex = decoder.resultError()
            throw ex
        }
    }

    @Test(expected = IllegalStateException::class)
    fun testDecodeError() {
        arrayOf(
            "",
            "ur:bytes/",
            "ur:ur:ur",
            "uf:bytes/hdeymejtswhhylkepmykhhtsytsnoyoyaxaedsuttydmmhhpktpmsrjtgwdpfnsboxgwlbaawzuefywkdplrsrjynbvygabwjldapfcsdwkbrkch"
        ).forEach {
            URDecoder.decode(it)
        }

    }
}