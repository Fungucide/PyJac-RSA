import java.math.BigInteger;
import java.security.MessageDigest;

import RSA.OAEP;
import RSA.RSA;
import RSA.Util;

public class Test {
	public static void main(String[] args) throws Exception {

		BigInteger n = new BigInteger(
				"26351208991538404124382559578995018177626454708173277487181926163495246731812489239767300043953357831433538346310742433463277341409587126486460215127913847");
		BigInteger m = new BigInteger(
				"26351208991538404124382559578995018177626454708173277487181926163495246731812164573890495059506704360936511580873365031724168373852796597753897491599011176");
		BigInteger e = new BigInteger("33441");
		BigInteger d = new BigInteger(
				"16939443787300053032560386449856640225006306535737595949342120951426611614298797938007953479089011203225148454114252806045705796259488904695300211623574153");
		byte[] pt = "hello".getBytes();
		byte[] maskedPt = OAEP.pad(pt, 512);
		System.out.println("Padded pt:" + Util.toHex(maskedPt));
		byte[] unmaskedPt = OAEP.unpad(maskedPt, 512);
		System.out.println("Unpadded pt:" + Util.toHex(unmaskedPt));
		System.out.println(new String(unmaskedPt));
	}
}
