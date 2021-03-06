package RSA;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A static class containing methods for OAEP padding. Source:
 * https://tools.ietf.org/html/rfc8017#section-7.1.1
 */
public class OAEP {

	/**
	 * Pads the data to a certain keySize using the OAEP SHA-1 protocol. It also
	 * uses a random seed for the random mask.
	 * 
	 * @param data    The data to be padded
	 * @param keySize The size of the key that will encrypt the data
	 * @return The masked data
	 * @throws Exception Thrown if the size of data is too large
	 */
	public static byte[] pad(byte[] data, int keySize) throws Exception {
		return pad(data, keySize, ThreadLocalRandom.current().nextLong());
	}

	/**
	 * Pads the data to a certain keySize using the OAEP SHA-1 protocol.
	 * 
	 * @param data    The data to be padded
	 * @param keySize The size of the key that will encrypt the data
	 * @param seed    The seed for the random mask
	 * @return The masked data
	 * @throws Exception Thrown if the size of data is too large
	 */
	public static byte[] pad(byte[] data, int keySize, long seed) throws Exception {
		Util.globalLog.stepIn("Pad: " + Util.toHex(data));
		int keySizeBytes = keySize / 8;
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		int hashSize = md.getDigestLength();
		int maxMessageSize = keySizeBytes - 2 * hashSize - 2;
		int padSize = maxMessageSize - data.length;
		Util.globalLog.log("Key Size (Bytes): " + keySizeBytes);
		Util.globalLog.log("Hash Size: " + hashSize);
		Util.globalLog.log("Message Size: " + data.length);
		Util.globalLog.log("Pad Size: " + padSize);
		if (data.length > maxMessageSize) {
			throw new Exception("Message too big after masking");
		}
		// Size of lHash || PS || 0x01 || M
		byte[] db = new byte[hashSize + padSize + 1 + data.length];
		// Assume optional label is left blank
		byte[] lHash = md.digest();
		Util.globalLog.log("Label Hash: " + Util.toHex(lHash));
		for (int i = 0; i < hashSize; i++) {
			db[i] = lHash[i];
		}
		// Leave padding 0x00 so that pad.length=keySize - 2*HASH_SIZE - data.length -2
		// Add the 0x01 to db
		db[hashSize + padSize] = 1;
		// Add the message to db
		for (int i = 0; i < data.length; i++) {
			db[hashSize + padSize + 1 + i] = data[i];
		}
		// Create a random string length HASH_SIZE and randomize its contents
		byte[] randomSeed = new byte[hashSize];
		SecureRandom.getInstanceStrong().setSeed(seed);
		SecureRandom.getInstanceStrong().nextBytes(randomSeed);
		Util.globalLog.log("Random Seed: " + Util.toHex(randomSeed));
		// The random mask from the mgf
		byte[] dbMask = maskGenerator(randomSeed, keySizeBytes - hashSize - 1, md);
		Util.globalLog.log("MGF(random): " + Util.toHex(dbMask));
		// XOR db and randomSeed
		for (int i = 0; i < db.length; i++) {
			db[i] = (byte) (db[i] ^ dbMask[i]);
		}

		byte[] seedMask = maskGenerator(db, hashSize, md);
		Util.globalLog.log("MGF(db): " + seedMask);
		for (int i = 0; i < seedMask.length; i++) {
			seedMask[i] = (byte) (seedMask[i] ^ randomSeed[i]);
		}
		// Concatenate 0x00 || db || seedMask to get the final result
		byte[] res = new byte[keySizeBytes];
		for (int i = 0; i < seedMask.length; i++) {
			res[i + 1] = seedMask[i];
		}
		for (int i = 0; i < db.length; i++) {
			res[seedMask.length + i + 1] = db[i];
		}
		Util.globalLog.log("Padded Data: " + Util.toHex(res));
		Util.globalLog.stepOut();
		return res;
	}

	/**
	 * Unpads the data using the OAEP SHA-1 Protocol.
	 * 
	 * @param data    The padded data
	 * @param keySize The size of the key used to encrypt the padded data
	 * @return The unpadded version of data
	 * @throws Exception Thrown if the data size is not equal to the key size or
	 *                   there was an error unpadding the message
	 */
	public static byte[] unpad(byte[] data, int keySize) throws Exception {
		Util.globalLog.stepIn("Unpad: " + Util.toHex(data));
		int keySizeBytes = keySize / 8;
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		if (data.length != keySizeBytes) {
			throw new Exception("Data size is not equal to key size");
		}
		int hashSize = md.getDigestLength();
		Util.globalLog.log("Key Size (Bytes): " + keySizeBytes);
		Util.globalLog.log("Hash Size: " + hashSize);
		Util.globalLog.log("Message Size: " + data.length);

		// Get the MGF of the db
		byte[] dbXor = new byte[data.length - hashSize - 1];
		for (int i = 0; i < dbXor.length; i++) {
			dbXor[i] = data[1 + hashSize + i];
		}
		byte[] hash = maskGenerator(dbXor, hashSize, md);
		Util.globalLog.log("MGF(db): " + Util.toHex(hash));
		// XOR the hash with seedMask to get seed
		byte[] randomSeed = new byte[hashSize];
		for (int i = 0; i < hashSize; i++) {
			randomSeed[i] = (byte) (hash[i] ^ data[1 + i]);
		}
		Util.globalLog.log("Random Seed: " + Util.toHex(randomSeed));
		// This is what the mask should have been when masking db
		byte[] dbMask = maskGenerator(randomSeed, keySizeBytes - hashSize - 1, md);
		Util.globalLog.log("MGF(random): " + Util.toHex(dbMask));
		byte[] db = new byte[data.length - hashSize - 1];
		for (int i = 0; i < db.length; i++) {
			db[i] = (byte) (data[1 + hashSize + i] ^ dbMask[i]);
		}
		// Once again assume label is blank
		byte[] labelHash = md.digest();
		for (int i = 0; i < labelHash.length; i++) {
			if (labelHash[i] != db[i]) {
				throw new Exception("The label used when padding was not blank.");
			}
		}
		int dataStart = labelHash.length;
		while (db[dataStart] == 0x00) {
			dataStart++;
		}
		if (db[dataStart] != 0x01) {
			throw new Exception("The start of the message was not marked with a 0x01 byte.");
		}
		dataStart++;
		byte[] res = new byte[db.length - dataStart];
		for (int i = 0; i < res.length; i++) {
			res[i] = db[dataStart + i];
		}
		Util.globalLog.log("Unpadded data: " + Util.toHex(res));
		Util.globalLog.stepOut();
		return res;
	}

	/**
	 * Mask generator function creates a mask based on the input. Source:
	 * https://en.wikipedia.org/wiki/Mask_generation_function
	 * 
	 * @param input  The input string to make the mask from
	 * @param length The length of the mask in bytes
	 * @param md     A class to hash the data
	 * @return A byte array containing a mask
	 */
	public static byte[] maskGenerator(byte[] input, int length, MessageDigest md) {
		byte[] res = new byte[length];
		int idx = 0;
		for (int i = 0; idx < res.length; i++) {
			md.update(input, 0, input.length);
			md.update(Util.I2OSP(i, 4), 0, 4);
			byte[] hash = md.digest();
			for (int j = 0; j < hash.length && idx < res.length; j++) {
				res[idx++] = hash[j];
			}
		}
		return res;
	}
}
