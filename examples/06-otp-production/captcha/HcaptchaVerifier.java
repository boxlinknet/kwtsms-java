import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * hCaptcha verifier. GDPR-safe alternative to reCAPTCHA.
 *
 * Setup:
 * 1. Sign up at https://www.hcaptcha.com/
 * 2. Create a sitekey
 * 3. Set HCAPTCHA_SECRET_KEY environment variable
 */
public class HcaptchaVerifier implements OtpService.CaptchaVerifier {

    private final String secretKey;

    public HcaptchaVerifier(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public boolean verify(String token, String remoteIp) {
        if (token == null || token.isEmpty()) return false;

        try {
            URL url = new URL("https://hcaptcha.com/siteverify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);

            String body = "secret=" + secretKey + "&response=" + token + "&remoteip=" + remoteIp;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return response.toString().contains("\"success\":true");
        } catch (Exception e) {
            return false;
        }
    }
}
