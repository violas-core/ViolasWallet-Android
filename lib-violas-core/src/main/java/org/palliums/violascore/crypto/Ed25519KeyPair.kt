package org.palliums.violascore.crypto

import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.palliums.violascore.serialization.toHex
import java.security.MessageDigest

class Ed25519PrivateKey(private val key: ByteArray) : KeyPair.PrivateKey {
    override fun toByteArray(): ByteArray = key
    fun toHex() = key.toHex()
}

class Ed25519PublicKey(private val key: ByteArray) : KeyPair.PublicKey {
    override fun toByteArray(): ByteArray = key
    fun toHex() = key.toHex()
}

class Signature(private val signature: ByteArray) {
    fun toByteArray(): ByteArray = signature
}

class Ed25519KeyPair(secretKey: ByteArray) {
    companion object {
        private val mDsaNamedCurveSpec =
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
    }

    private val mEdDSAPrivateKey by lazy {
        EdDSAPrivateKey(EdDSAPrivateKeySpec(secretKey, mDsaNamedCurveSpec))
    }
    private val mEdDSAPublicKey by lazy {
        EdDSAPublicKey(EdDSAPublicKeySpec(mEdDSAPrivateKey.a, mEdDSAPrivateKey.params))
    }

    fun getPublicKey(): Ed25519PublicKey {
        return Ed25519PublicKey(mEdDSAPublicKey.abyte)
    }

    fun signMessage(message: ByteArray): Signature {
        val edDSAEngine = EdDSAEngine(MessageDigest.getInstance(mDsaNamedCurveSpec.hashAlgorithm))
        edDSAEngine.initSign(mEdDSAPrivateKey)
        edDSAEngine.update(message)
        return Signature(edDSAEngine.sign())
    }

    fun verify(signed: Signature, message: ByteArray): Boolean {
        val edDSAEngine = EdDSAEngine(MessageDigest.getInstance(mDsaNamedCurveSpec.hashAlgorithm))
        edDSAEngine.initVerify(mEdDSAPublicKey)
        edDSAEngine.update(message)
        return edDSAEngine.verify(signed.toByteArray())
    }
}

