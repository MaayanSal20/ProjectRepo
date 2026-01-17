package Server;

import java.security.SecureRandom;

public class ScanCodeGenerator {
    private static final SecureRandom RND = new SecureRandom();

    public static String generate() {
        int code = 10000000 + RND.nextInt(90000000);
        return String.valueOf(code);
    }
}
