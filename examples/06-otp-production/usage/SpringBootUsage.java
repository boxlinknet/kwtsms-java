import com.kwtsms.*;

/**
 * Spring Boot integration example for production OTP.
 *
 * Uncomment the Spring annotations to use in a real Spring Boot project.
 */
// @RestController
// @RequestMapping("/api/otp")
public class SpringBootUsage {

    private final OtpService otpService;

    public SpringBootUsage() {
        KwtSMS sms = KwtSMS.fromEnv();
        OtpService.OtpStore store = new MemoryStore(); // Replace with RedisStore in production
        OtpService.CaptchaVerifier captcha = new TurnstileVerifier(System.getenv("TURNSTILE_SECRET_KEY"));
        this.otpService = new OtpService(sms, store, captcha, "MyApp");
    }

    // @PostMapping("/send")
    public Object sendOtp(/* @RequestBody */ java.util.Map<String, String> body,
                          /* HttpServletRequest */ String remoteIp) {
        String phone = body.get("phone");
        String captchaToken = body.get("captchaToken");

        OtpService.OtpResult result = otpService.sendOtp(phone, remoteIp, captchaToken);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", result.success);
        if (!result.success) {
            response.put("error", result.error);
        }
        return response;
    }

    // @PostMapping("/verify")
    public Object verifyOtp(/* @RequestBody */ java.util.Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");

        OtpService.OtpResult result = otpService.verifyOtp(phone, code);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", result.success);
        if (!result.success) {
            response.put("error", result.error);
        }
        return response;
    }
}
