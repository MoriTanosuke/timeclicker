package de.kopis.timeclicker.model.wrappers;

import java.io.Serializable;

public class Project implements Serializable {
  public final String name;

  public Project(String name) {
    this.name = name;
  }
}
