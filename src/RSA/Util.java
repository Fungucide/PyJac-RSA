package RSA;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import ui.StepTreeView;

public class Util {

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	private static final int PRIMALITY_CHECK_ITERATIONS = 50;
	public static StepTreeView primeLog = null;
	public static StepTreeView globalLog = null;

	private static void log(String message) {
		if (globalLog != null) {
			globalLog.log(message);
		}
	}

	private static void stepIn(String message) {
		if (globalLog != null) {
			globalLog.stepIn(message);
		}
	}

	private static void stepOut() {
		if (globalLog != null) {
			globalLog.stepOut();
		}
	}

	/**
	 * Returns a random prime BigInteger of size bits. The first and second bits are
	 * guaranteed to be set.
	 * 
	 * @param bits The size of the prime BigInteger that will be returned
	 * @return A random prime BigInteger of size bits
	 */
	public static BigInteger randomPrime(int bits) {
		BigInteger prime;
		// Generate random number and repeat if it is not prime
		do {
			// Make sure the number is odd since we know even numbers other than 2 are not
			// prime
			prime = randomBigInteger(bits).setBit(0);
		} while (!isPrime(prime));
		return prime;
	}

	/**
	 * Returns a random BigInteger of size bits.
	 * 
	 * @param bits The size of the BigInteger that will be returned
	 * @return A random BigInteger of size bits
	 */
	public static BigInteger randomBigInteger(int bits) {
		BigInteger res = new BigInteger(bits, new Random()).setBit(bits - 1).setBit(bits - 2);
		return res;
	}

	/**
	 * Returns true if there is a very high chance that n is a prime number. Source:
	 * http://home.sandiego.edu/~dhoffoss/teaching/cryptography/10-Rabin-Miller.pdf
	 * 
	 * @param n Some BigInteger to be tested
	 * @return If n is probably a prime number
	 */
	public static boolean isPrime(BigInteger n) {
		log("Check if " + n.toString() + " is prime.");
		// Use some other function if n is sufficiently small (n<=10^10)
		if (n.compareTo(BigInteger.valueOf(10000000000l)) <= 0) {
			log("The number " + n.toString() + " is small enough to be checked normally.");
			return isPrime(n.longValue());
		} else if (!n.testBit(0)) {
			log("The number " + n.toString() + " is even so it is composite.");
			return false;
		}
		log("The number " + n.toString() + "is too large and will be checked with Rabin Miller primality test.");

		// Find values to the equation n=2^s*m where s is as large as possible
		int s = 0;
		// Find out how many times we can divide (n-1) by 2
		BigInteger nMinusOne = n.subtract(BigInteger.ONE);
		while (!nMinusOne.testBit(s)) {
			s++;
		}
		// Find k by calculating n>>s
		BigInteger m = nMinusOne.shiftRight(s);
		log(n.toString() + " - 1 = 2^" + s + " * " + m.toString());

		int prime = 0;
		int composite = 0;

		// Check to see if n is prime using base a
		long maxValue = n.bitLength() > 63 ? Long.MAX_VALUE : n.longValue();
		for (int i = 0; i < PRIMALITY_CHECK_ITERATIONS; i++) {
			stepIn("Trial " + i);
			BigInteger a = BigInteger.valueOf(randomLong(2, maxValue - 2));
			log("Checking if n is prime using a=" + a.toString() + ".");
			// First iteration is a^m % n
			BigInteger res = a.modPow(m, n);
			log("a^m % n = " + res.toString());
			// On first iteration if |a^m mod n| = 1 then say it is prime
			if (res.equals(BigInteger.ONE) || res.equals(nMinusOne)) {
				log("We can declare that " + n.toString() + " is prime.");
				stepOut();
				prime++;
				continue;
			}
			boolean flag = false;
			for (int j = 1; !flag && j < s; j++) {
				res = res.modPow(BigInteger.TWO, n);
				log("a^(2^" + j + " * m) % n = " + res.toString());
				if (res.equals(BigInteger.ONE)) {
					// If a^[(2^j)*m] % n = 1 then we say is is composite
					log("We can declare that " + n.toString() + " is composite.");
					composite++;
					flag = true;
				} else if (res.equals(nMinusOne)) {
					log("We can declare that " + n.toString() + " is prime.");
					// If a^[(2^j)*m] % n = -1 then we say is is prime
					prime++;
					flag = true;
				}
			}
			if (!flag) {
				log("We can declare that " + n.toString() + " is composite.");
				composite++;
			}
			stepOut();
		}
		log("Out of " + (prime + composite) + " trials, " + prime + " trials claimed n was prime.");
		log("n is prime: " + (prime > composite));
		return prime > composite;
	}

	/**
	 * Returns true if n is prime. Precondition: n <= 10^10 so the code actually
	 * runs in time.
	 * 
	 * @param n The number n
	 * @return If n is a prime number
	 */
	private static boolean isPrime(long n) {
		if (n < 2) {
			return false;
		} else if (n == 2) {
			return true;
		}
		boolean isPrime = (n % 2l == 1);
		// Check n against all odd numbers greater than 2
		for (int i = 3; isPrime && i * i <= n; i += 2) {
			// If n is divisible by i then n is not prime
			if (n % i == 0) {
				isPrime = false;
			}
		}
		return isPrime;
	}

	/**
	 * Returns a random long in [lower, upper).
	 * 
	 * @param lower lower bound
	 * @param upper upper bound
	 * @return A pseudo-random long in [lower, upper)
	 */
	private static long randomLong(long lower, long upper) {
		return ThreadLocalRandom.current().nextLong(lower, upper);
	}

	/**
	 * Returns the hex representation of the byte array as a string.
	 * 
	 * @param data the data to be represented
	 * @return A hex string that represents data
	 */
	public static String toHex(byte[] data) {
		StringBuilder builder = new StringBuilder(data.length * 3);
		for (byte b : data) {
			int v = b & 0xFF;
			// Get the first then second character
			builder.append(HEX_ARRAY[v >>> 4]);
			builder.append(HEX_ARRAY[v & 0x0F]);
			builder.append(' ');
		}
		return builder.toString().strip();
	}

	/**
	 * Converts a hex string to a byte array.
	 * 
	 * @param hex The hex string to be converted
	 * @return The resulting byte array from the hex string
	 */
	public static byte[] toAscii(String hex) {
		hex = hex.toLowerCase().replaceAll("[^0-9a-f]", "");
		byte[] res = new byte[hex.length() >> 1];
		byte currentByte = 0;
		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(i);
			if ('0' <= c && c <= '9') {
				currentByte |= (byte) ((c - '0') << ((i + 1) & 1) * 4);
			} else if ('a' <= c && c <= 'f') {
				currentByte |= (byte) ((10 + c - 'a') << ((i + 1) & 1) * 4);
			}
			if ((i & 1) == 1) {
				res[i >> 1] = currentByte;
				currentByte = 0;
			}
		}
		return res;
	}

	public static byte[] I2OSP(int value, int length) {
		byte[] res = new byte[length];
		for (int i = 0; i < length; i++) {
			res[length - i - 1] = (byte) ((value >> (i << 3)) & 0xFF);
		}
		return res;
	}
}
