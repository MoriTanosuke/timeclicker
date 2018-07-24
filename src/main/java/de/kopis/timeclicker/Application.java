package de.kopis.timeclicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SpringBootApplication(scanBasePackages = {"de.kopis.timeclicker"})
public class Application implements WebMvcConfigurer {
  public static final int HOURS_PER_DAY = 8;
  public static final int HOURS_PER_DAY_IN_MILLISECONDS = HOURS_PER_DAY * 60 * 60 * 1000;

  // TODO get locale from user settings
  public static final Locale LOCALE = Locale.getDefault();
  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN, LOCALE);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
