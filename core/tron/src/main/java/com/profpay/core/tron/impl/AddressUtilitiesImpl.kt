// core/tron/src/main/java/com/profpay/core/tron/impl/AddressUtilitiesImpl.kt
package com.profpay.core.tron.impl

import android.util.Log
import androidx.annotation.VisibleForTesting
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toEntropy
import cash.z.ecc.android.bip39.toSeed
import com.profpay.core.tron.api.TronAddressApi
import com.profpay.core.tron.model.AddressDataWithPrivKey
import com.profpay.core.tron.model.AddressDataWithoutPrivKey
import com.profpay.core.tron.model.AddressGenerateFromSeedPhr
import com.profpay.core.tron.model.AddressGenerateResult
import com.profpay.core.tron.model.AddressesWithKeysForM
import com.profpay.core.tron.model.DerivedKeyData
import com.profpay.core.tron.network.TronNodeManager
import kotlinx.coroutines.withTimeout
import org.bitcoinj.base.Base58
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicHierarchy
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.HDPath
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.key.KeyPair
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class AddressUtilitiesImpl @Inject constructor() : TronAddressApi {

    // ══════════════════════════════════════════════════════════════════════
    // Validation
    // ══════════════════════════════════════════════════════════════════════

    override fun isValidTronAddress(address: String): Boolean {
        return try {
            if (!address.matches(Regex("^T[1-9A-HJ-NP-Za-km-z]{33}$"))) return false

            val decoded = Base58.decode(address)
            if (decoded.size != 25) return false
            if (decoded[0] != 0x41.toByte()) return false

            val checksum = decoded.takeLast(4).toByteArray()
            val data = decoded.dropLast(4).toByteArray()

            val sha256Once = MessageDigest.getInstance("SHA-256").digest(data)
            val sha256Twice = MessageDigest.getInstance("SHA-256").digest(sha256Once)
            val calculatedChecksum = sha256Twice.take(4).toByteArray()

            checksum.contentEquals(calculatedChecksum)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun isAddressActivated(address: String): Boolean {
        return TronNodeManager.executeWithFailoverSuspend { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey()
            )

            try {
                withTimeout(5000.milliseconds) {
                    val res = wrapper.getAccount(address)
                    res.activePermissionList.isNotEmpty()
                }
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun isContractAddress(address: String): Boolean {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey()
            )

            try {
                val contract = wrapper.getContract(address)
                contract.bytecode != null && !contract.bytecode.isEmpty
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Balances
    // ══════════════════════════════════════════════════════════════════════

    override fun getTrxBalance(address: String): BigInteger {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey()
            )

            try {
                BigInteger.valueOf(wrapper.getAccountBalance(address))
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun getUsdtBalance(address: String): BigInteger {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey()
            )

            try {
                val contract: Contract = wrapper.getContract(USDT_CONTRACT_ADDRESS)
                val token = Trc20Contract(contract, USDT_OWNER_ADDRESS, wrapper)
                token.balanceOf(address)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Chain Parameters
    // ══════════════════════════════════════════════════════════════════════

    override fun getCreateNewAccountFeeInSystemContract(): BigInteger {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey()
            )

            try {
                for (chainParameter in wrapper.chainParameters.chainParameterList) {
                    if (chainParameter.key == "getCreateNewAccountFeeInSystemContract") {
                        return@executeWithFailover BigInteger.valueOf(chainParameter.value)
                    }
                }
                BigInteger.ZERO
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Key Derivation
    // ══════════════════════════════════════════════════════════════════════

    override fun generateAddressAndMnemonic(): AddressGenerateResult {
        val entropy: ByteArray = Mnemonics.WordCount.COUNT_12.toEntropy()
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)

        val addressDataList = mutableListOf<AddressDataWithoutPrivKey>()

        repeat(7) { index ->
            val key = generateKeys(mnemonicCode.toSeed(validate = true), index)
            val address = publicKeyToAddress(key.pubKeyPoint.getEncoded(false))
                ?: throw IllegalStateException("Failed to create public address")

            addressDataList.add(
                AddressDataWithoutPrivKey(
                    address = address,
                    publicKey = key.publicKeyAsHex,
                    indexDerivationSot = index,
                    indexSot = index.toByte(),
                ),
            )
        }

        // Конвертируем слова из CharArray в List<String>
        val mnemonicWords = mnemonicCode.words.map { String(it) }
        val mnemonicPhrase = mnemonicWords.joinToString(" ")

        return AddressGenerateResult(
            addressesWithKeysForM = AddressesWithKeysForM(
                addresses = addressDataList,
                entropy = entropy,
                derivedIndices = (1..6).toList(),
            ),
            mnemonicWords = mnemonicWords,
            mnemonicPhrase = mnemonicPhrase,
        )
    }

    override fun generateSingleAddress(): AddressDataWithPrivKey {
        val entropy: ByteArray = Mnemonics.WordCount.COUNT_12.toEntropy()
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val deterministicKey = generateKeys(mnemonicCode.toSeed(validate = true), 0)

        val address = publicKeyToAddress(deterministicKey.pubKeyPoint.getEncoded(false))
            ?: throw IllegalStateException("Failed to create public address")

        return AddressDataWithPrivKey(
            address = address,
            publicKey = deterministicKey.publicKeyAsHex,
            privateKey = deterministicKey.privateKeyAsHex,
        )
    }

    override fun getGeneralAddressBySeedPhrase(seed: String): String {
        val mnemonicCode = Mnemonics.MnemonicCode(chars = seed.toCharArray())

        return try {
            val key = generateKeys(mnemonicCode.toSeed(validate = true), 0)
            publicKeyToAddress(key.pubKeyPoint.getEncoded(false))
                ?: throw IllegalStateException("Failed to create public address")
        } catch (_: Exception) {
            throw IllegalArgumentException("Failed generate, may be incorrect mnemonic")
        }
    }

    override fun recoveryKeysAndAddressBySeedPhrase(
        seed: String,
        derivedIndices: List<Int>,
    ): AddressGenerateFromSeedPhr {
        val mnemonicCode = Mnemonics.MnemonicCode(chars = seed.toCharArray())
        val addressList = mutableListOf<AddressDataWithoutPrivKey>()
        val derivedIndicesWithZero = listOf(0) + derivedIndices

        try {
            derivedIndicesWithZero.forEachIndexed { index, item ->
                addressList.add(generateAddressData(mnemonicCode, item, index.toByte()))
            }

            getArchiveSots(derivedIndices).forEach { item ->
                addressList.add(generateAddressData(mnemonicCode, item, -1))
            }
        } catch (_: Exception) {
            throw IllegalArgumentException("Failed generate, may be incorrect mnemonic")
        }

        return AddressGenerateFromSeedPhr(
            AddressesWithKeysForM(
                addresses = addressList,
                entropy = mnemonicCode.toEntropy(),
                derivedIndices = derivedIndicesWithZero.filter { it != 0 },
            )
        )
    }

    override suspend fun generateNextAddressGroup(seed: String): AddressGenerateFromSeedPhr {
        val mnemonic = Mnemonics.MnemonicCode(seed.toCharArray())
        val resultList = mutableListOf<AddressDataWithoutPrivKey>()

        // 0-й индекс всегда включаем
        resultList.add(generateAddressData(mnemonic, 0, 0))

        var index = 1
        var foundIndex: Int

        while (true) {
            val currentAddr = generateAddressData(mnemonic, index, index.toByte())
            val currentActivated = isAddressActivated(currentAddr.address)

            if (currentActivated) {
                index++
                continue
            }

            // Проверяем следующие 5 адресов (всего 6)
            var allSixInactive = true
            for (offset in 1..5) {
                val nextAddr = generateAddressData(mnemonic, index + offset, (index + offset).toByte())
                if (isAddressActivated(nextAddr.address)) {
                    allSixInactive = false
                    break
                }
            }

            if (allSixInactive) {
                foundIndex = index
                break
            }

            index++
        }

        val derivedIndices = (listOf(0) + (foundIndex until foundIndex + 6))
        val archiveIndices = getArchiveSots(derivedIndices)

        // Добавляем найденные 6 адресов
        (foundIndex until foundIndex + 6).forEach { idx ->
            resultList.add(generateAddressData(mnemonic, idx, idx.toByte()))
        }

        // Добавляем архивные
        archiveIndices.forEach { idx ->
            resultList.add(generateAddressData(mnemonic, idx, -1))
        }

        return AddressGenerateFromSeedPhr(
            AddressesWithKeysForM(
                addresses = resultList,
                entropy = mnemonic.toEntropy(),
                derivedIndices = derivedIndices
            )
        )
    }

    override fun derivePrivateKeyFromEntropy(entropy: ByteArray, index: Int): ByteArray {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val seed = mnemonicCode.toSeed(validate = true)
        val key = generateKeys(seed, index)
        return key.privKeyBytes
    }

    override fun deriveHexPrivateKeyFromEntropy(entropy: ByteArray, index: Int): String {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val seed = mnemonicCode.toSeed(validate = true)
        val key = generateKeys(seed, index)
        return key.privateKeyAsHex
    }

    override fun getSeedPhraseByEntropy(entropy: ByteArray): String {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        return mnemonicCode.words.joinToString(" ") { String(it) }
    }

    /**
     * Деривирует детерминированный ключ по BIP-44 для Tron.
     *
     * Путь деривации: M/44H/195H/0H/0/{index}
     *
     * @param entropy Энтропия из мнемонической фразы.
     * @param index Индекс адреса в HD-пути.
     * @return [DeterministicKey] с публичным и приватным ключом.
     */
    override fun deriveKeyAtIndex(
        entropy: ByteArray,
        index: Long,
    ): DerivedKeyData {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val masterKey = HDKeyDerivation.createMasterPrivateKey(mnemonicCode.toSeed(validate = true))
        val hierarchy = DeterministicHierarchy(masterKey)
        val path: List<ChildNumber> = HDPath.parsePath("M/44H/195H/0H/0/$index")
        val key = hierarchy.get(path, true, true)

        return DerivedKeyData(
            publicKeyHex = key.publicKeyAsHex,
            publicKeyBytes = key.pubKeyPoint.getEncoded(false),
        )
    }

    // ══════════════════════════════════════════════════════════════════════
    // Address Conversion
    // ══════════════════════════════════════════════════════════════════════

    override fun hexToBase58CheckAddress(hex: String): String {
        val hexBytes = hex.hexStringToByteArray()
        val checksum = sha256(sha256(hexBytes)).copyOfRange(0, 4)
        return Base58.encode(hexBytes + checksum)
    }

    override fun publicKeyToAddress(publicKey: ByteArray): String? {
        val hash = sha3(publicKey.copyOfRange(1, publicKey.size))
        val address = hash?.copyOfRange(11, hash.size) ?: return null

        address[0] = 65 // T symbol (0x41)

        val salt = sha256(sha256(address))
        val inputCheck = ByteArray(address.size + 4)

        System.arraycopy(address, 0, inputCheck, 0, address.size)
        System.arraycopy(salt, 0, inputCheck, address.size, 4)
        return Base58.encode(inputCheck)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════

    private fun generateKeys(seed: ByteArray, index: Int): DeterministicKey {
        val masterKey = HDKeyDerivation.createMasterPrivateKey(seed)
        val hierarchy = DeterministicHierarchy(masterKey)
        val path: List<ChildNumber> = HDPath.parsePath("M/44H/195H/0H/0/$index")
        return hierarchy.get(path, true, true)
    }

    @VisibleForTesting
    internal fun generateAddressData(
        mnemonicCode: Mnemonics.MnemonicCode,
        index: Int,
        indexSot: Byte,
    ): AddressDataWithoutPrivKey {
        val key = generateKeys(mnemonicCode.toSeed(validate = true), index)
        val address = publicKeyToAddress(key.pubKeyPoint.getEncoded(false))
            ?: throw IllegalStateException("Failed to create public address")

        return AddressDataWithoutPrivKey(
            address = address,
            publicKey = key.publicKeyAsHex,
            indexDerivationSot = index,
            indexSot = indexSot,
        )
    }

    private fun getArchiveSots(derivedIndices: List<Int>): List<Int> {
        val maxIndex = derivedIndices.maxOrNull() ?: return emptyList()
        val indexSet = derivedIndices.toSet()
        return (1 until maxIndex).filter { it !in indexSet }
    }

    private fun sha256(input: ByteArray): ByteArray =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(input)
            digest.digest()
        } catch (err: NoSuchAlgorithmException) {
            throw RuntimeException(err)
        }

    private fun sha3(input: ByteArray): ByteArray? {
        val kecc: Keccak.DigestKeccak = Keccak.Digest256()
        kecc.update(input)
        return kecc.digest()
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun safeClose(wrapper: ApiWrapper) {
        try {
            wrapper.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close ApiWrapper: $e")
        }
    }

    private companion object {
        const val TAG = "AddressUtilities"
        const val USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        const val USDT_OWNER_ADDRESS = "TJJaVcRremausriMLkZeRedM95v7HW4j4D"
    }
}
