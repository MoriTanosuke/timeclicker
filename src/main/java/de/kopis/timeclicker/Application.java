package de.kopis.timeclicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SpringBootApplication(scanBasePackages = {"de.kopis.timeclicker"})
public class Application implements WebMvcConfigurer {
  // TODO get locale from user settings
  public static final Locale LOCALE = Locale.getDefault();
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  public static DateFormat getDateFormat() {
    return new SimpleDateFormat(DATE_PATTERN, LOCALE);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
