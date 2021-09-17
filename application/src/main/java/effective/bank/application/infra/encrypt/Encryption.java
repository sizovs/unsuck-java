package effective.bank.application.infra.encrypt;

import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Encryption {

  private static final Logger logger = LoggerFactory.getLogger(Encryption.class);

  private static TextEncryptor ENCRYPTOR = new WeakTextEncryptor();

  private Encryption(
      @Value("${security.encryption.password:#{null}") Optional<String> encryptionPwd) {
    ENCRYPTOR =
        encryptionPwd
            .map(
                pwd -> {
                  var encryptor = new BasicTextEncryptor();
                  encryptor.setPassword(pwd);
                  return (TextEncryptor) encryptor;
                })
            .orElseGet(WeakTextEncryptor::new);
  }

  public static String encrypt(String rawValue) {
    return ENCRYPTOR.encrypt(rawValue);
  }

  public static String decrypt(String encValue) {
    return ENCRYPTOR.decrypt(encValue);
  }

  private static class WeakTextEncryptor implements TextEncryptor {

    private static final String HARD_CODED_ENCRYPTION_PWD = "12345";

    private final BasicTextEncryptor encryptor;

    WeakTextEncryptor() {
      encryptor = new BasicTextEncryptor();
      encryptor.setPassword(HARD_CODED_ENCRYPTION_PWD);
    }

    @Override
    public String encrypt(String message) {
      logger.warn(
          "Encryption is not configured and is not production-ready. Hard-coded encryption password {} will be used",
          HARD_CODED_ENCRYPTION_PWD);
      return encryptor.encrypt(message);
    }

    @Override
    public String decrypt(String encryptedMessage) {
      logger.warn(
          "Encryption is not configured and is not production-ready. Hard-coded encryption password {} will be used",
          HARD_CODED_ENCRYPTION_PWD);
      return encryptor.decrypt(encryptedMessage);
    }
  }
}