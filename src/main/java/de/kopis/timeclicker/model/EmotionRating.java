package de.kopis.timeclicker.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

public class EmotionRating implements Serializable {

  public static final String EMOTION_RATING_USER_ID = "userId";
  public static final String EMOTION_RATING_DATE = "date";
  public static final String EMOTION_RATING_EMOTION = "emotion";
  public static final String EMOTION_RATING_ENTITY = "EmotionRating";

  public enum Emotion {
    GOOD,
    NEUTRAL,
    BAD
  }

  private static final long serialVersionUID = 1L;

  private String key;
  private Instant date;
  private Emotion emotion;

  public EmotionRating() {
    this.date = Instant.now();
  }

  public EmotionRating(String key, Instant date, Emotion emotion) {
    this(date, emotion);
    this.key = key;
  }

  public EmotionRating(Instant date, Emotion emotion) {
    this.date = date;
    this.emotion = emotion;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Instant getDate() {
    return date;
  }

  public Date getAsDate() {
    return Date.from(date);
  }

  public void setDate(Instant date) {
    this.date = date;
  }

  public void setDate(Date date) {
    this.date = date.toInstant();
  }

  public Emotion getEmotion() {
    return emotion;
  }

  public void setEmotion(Emotion emotion) {
    this.emotion = emotion;
  }

  @Override
  public String toString() {
    return "EmotionRating{" +
        "key='" + key + '\'' +
        ", date=" + date +
        ", emotion=" + emotion +
        '}';
  }
}
