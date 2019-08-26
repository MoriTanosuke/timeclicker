package de.kopis.timeclicker.exceptions;

public class RatingNotOwnedByUserException extends Exception {
  public RatingNotOwnedByUserException() {
    super("Entity does not belong to current user");
  }
}
