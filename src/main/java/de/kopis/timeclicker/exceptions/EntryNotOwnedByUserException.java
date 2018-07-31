package de.kopis.timeclicker.exceptions;

public class EntryNotOwnedByUserException extends Exception {
  public EntryNotOwnedByUserException() {
    super("Entity does not belong to current user");
  }
}
