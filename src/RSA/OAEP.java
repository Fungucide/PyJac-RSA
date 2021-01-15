package RSA;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class OAEP {

	public static byte[] pad(byte[] data, int keySize) throws Exception {
		return pad(data, keySize, ThreadLocalRandom.current().nextLong());
	}

	public static byte[] pad(byte[] data, int keySize, long seed) throws Exception {
		int keySizeBytes = keySize / 8;
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		int hashSize = md.getDigestLength();
		int maxMessageSize = keySizeBytes - 2 * hashSize - 2;
		int padSize = maxMessageSize - data.length;
		if (data.length > maxMessageSize) {
			throw new Exception("Message too big after masking");
		}
		// Size of lHash || PS || 0x01 || M
		byte[] db = new byte[hashSize + padSize + 1 + data.length];
		// Assume optional label is left blank
		byte[] lHash = md.digest();
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
		randomSeed = Util.toAscii("AFA492BED3EA8D8CAA0B9D19E104AEAED7265FDF");
		// The random mask from the mgf
		byte[] dbMask = maskGenerator(randomSeed, keySizeBytes - hashSize - 1, md);
		// XOR db and randomSeed
		for (int i = 0; i < db.length; i++) {
			db[i] = (byte) (db[i] ^ dbMask[i]);
		}

		byte[] seedMask = maskGenerator(db, hashSize, md);
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
		return res;
	}

	public static byte[] unpad(byte[] data, int keySize) throws Exception {
		int keySizeBytes = keySize / 8;
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		if (data.length != keySizeBytes) {
			throw new Exception("Data size is not equal to key size");
		}
		int hashSize = md.getDigestLength();
		// Get the MGF of the db
		byte[] dbXor = new byte[data.length - hashSize - 1];
		for (int i = 0; i < dbXor.length; i++) {
			dbXor[i] = data[1 + hashSize + i];
		}
		byte[] hash = maskGenerator(dbXor, hashSize, md);
		// XOR the hash with seedMask to get seed
		byte[] randomSeed = new byte[hashSize];
		for (int i = 0; i < hashSize; i++) {
			randomSeed[i] = (byte) (hash[i] ^ data[1 + i]);
		}
		// This is what the mask should have been when masking db
		byte[] dbMask = maskGenerator(randomSeed, keySizeBytes - hashSize - 1, md);
		byte[] db = new byte[data.length - hashSize - 1];
		for (int i = 0; i < db.length; i++) {
			db[i] = (byte) (data[1 + hashSize + i] ^ dbMask[i]);
		}
		// Once again assume label is blank
		byte[] labelHash = md.digest();
		for (int i = 0; i < labelHash.length; i++) {
			if (labelHash[i] != db[i]) {
				throw new Exception("Something went wrong when unpadding.");
			}
		}
		int dataStart = labelHash.length;
		while (db[dataStart] == 0x00) {
			dataStart++;
		}
		if (db[dataStart] != 0x01) {
			throw new Exception("Something went wrong when unpadding.");
		}
		dataStart++;
		byte[] res = new byte[db.length - dataStart];
		for (int i = 0; i < res.length; i++) {
			res[i] = db[dataStart + i];
		}
		return res;
	}

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
