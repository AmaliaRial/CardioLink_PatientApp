package jdbc;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Encryption {

    // Encrypt (client side)
    public static String encrypt(String data, String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = factory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    // Decrypt (server side)
    public static String decrypt(String encryptedData, String base64PrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = factory.generatePrivate(spec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }

    // Generate key pair (you can call this once to generate keys)
    public static void generateKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        System.out.println("Public key (send to clients):");
        System.out.println(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        System.out.println("\nPrivate key (keep on server only):");
        System.out.println(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
    }

    //TODO: RUN ONCE Encryption.generateKeys() TO GET A KEY PAIR
    //done in Public_Encryption_Keys.txt
    //Copy the public key for the client and private key for your server.

}
