package Server;

import java.security.SecureRandom;

/**
 * Generates random 8-digit scan codes for subscriber identification.
 */
public class ScanCodeGenerator {

    /** Secure random generator */
    private static final SecureRandom RND = new SecureRandom();

    /**
     * Generates a random 8-digit scan code.
     *
     * @return generated scan code as a string
     */
    public static String generate() {
        int code = 10000000 + RND.nextInt(90000000);
        return String.valueOf(code);
    }
}
