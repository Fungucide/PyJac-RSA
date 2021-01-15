package RSA;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RSA {

	/*
	 * https://www.scribd.com/document/490155524/MAT315-Presentation SHA-OEAP sha512
	 */

	public static byte[] decrypt(byte[] data, BigInteger publicKey, BigInteger privateExponent) throws Exception {
		// Catch invalid public keys
		if (publicKey.compareTo(BigInteger.ZERO) < 0) {
			throw new Exception("Public key should not be negative.");
		} else if (publicKey.equals(BigInteger.ZERO)) {
			throw new Exception("Public key should not be 0.");
		}
		// Convert the data to a single BigInteger
		BigInteger cyphertext = new BigInteger(data);
		if (cyphertext.compareTo(publicKey) >= 0) {
			throw new Exception("Key too small for data");
		}
		return cyphertext.modPow(privateExponent, publicKey).toByteArray();
	}

	/**
	 * Encrypts the data based on the publicKey and publicExponent
	 * 
	 * @param data           A byte array with the data to be encrypted
	 * @param publicKey      The public key that the data is going to be encrypted
	 *                       with
	 * @param publicExponent The public exponent that the data is going to be
	 *                       encrypted with
	 * @return The encrypted value of data
	 * @throws Exception Thrown if a bad public key is received or if the data is
	 *                   not properly padded
	 */
	public static byte[] encrypt(byte[] data, BigInteger publicKey, BigInteger publicExponent) throws Exception {
		// Catch invalid public keys
		if (publicKey.compareTo(BigInteger.ZERO) < 0) {
			throw new Exception("Public key should not be negative.");
		} else if (publicKey.equals(BigInteger.ZERO)) {
			throw new Exception("Public key should not be 0.");
		}
		// Convert the data to a single BigInteger
		BigInteger plaintext = new BigInteger(data);
		if (plaintext.compareTo(publicKey) >= 0) {
			throw new Exception("Key too small for data");
		}
		return plaintext.modPow(publicExponent, publicKey).toByteArray();
	}

}
