import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import RSA.RSA;
import RSA.Util;

class RSATest {

	@Test
	public void isPrimeTest() {
		assertTrue(Util.isPrime(new BigInteger("104513")));
		assertTrue(Util.isPrime(new BigInteger("7")));
		assertTrue(Util.isPrime(new BigInteger("269432034627817064305613273322054264967")));
	}

	@Test
	public void randomPrimeTest() {
		for (int i = 0; i < 100; i++) {
			BigInteger prime = Util.randomPrime(128);
			assertEquals(prime.bitLength(), 128);
			assertTrue(Util.isPrime(prime), prime.toString());
		}
	}

	@Test
	public void encryptDecryptTest() throws Exception {
		BigInteger p = Util.randomPrime(256);
		BigInteger q = Util.randomPrime(256);
		BigInteger n = p.multiply(q);
		BigInteger totientN = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		BigInteger e = Util.randomBigInteger(16);
		// Make sure that e and m are relatively prime
		if (!e.gcd(totientN).equals(BigInteger.ONE)) {
			e = e.divide(e.gcd(totientN));
		}
		BigInteger d = e.modInverse(totientN);
		System.out.println(e.gcd(totientN).toString());
		System.out.println(e.multiply(d).mod(totientN).toString());
		byte[] data = "some message".getBytes();
		byte[] cypherText = RSA.encrypt(data, n, e);
		byte[] plainText = RSA.decrypt(cypherText, n, d);
		assertArrayEquals(plainText, data);
	}

	@Test
	public void toHexAndBackTest() {
		byte[] arr = new byte[256];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (byte) i;
		}
		String hexString = Util.toHex(arr);
		byte[] res = Util.toAscii(hexString);
		assertArrayEquals(arr, res);
	}
}
