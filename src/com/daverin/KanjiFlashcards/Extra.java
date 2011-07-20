package com.daverin.KanjiFlashcards;

import java.io.Serializable;
import java.util.UUID;

import android.content.Intent;

/**
 * Represents a value that can be passed around as an {@code Intent} extra. An
 * instance contains the key name of the extra, and a way to store it in an
 * {@code Intent} (see the {@link #put(Intent, T)} and {@link #get(Intent)}
 * methods). The key name is guaranteed to be unique and is self-generated.
 * 
 * @author Matt DeVore
 *
 * @param <T> the type of the extra value
 */
public abstract class Extra<T> {
  private static final String PACKAGE = "com.daverin.KanjiFlashcards.";
  
  private static class SerializableExtra<T extends Serializable>
      extends Extra<T> {
    @SuppressWarnings("unchecked")
    @Override
    public T get(Intent i) {
      return (T)i.getSerializableExtra(name_);
    }

    @Override
    public void put(Intent i, T value) {
      i.putExtra(name_, (Serializable)value);
    }
  }
  
  /**
   * Creates a unique instance which stores values in an {@code Intent} using
   * Java serialization.
   * 
   * @param <T> the type of the extra value
   * @return a unique {@code Extra} instance
   */
  public static <T extends Serializable> Extra<T> forSerializable() {
    return new SerializableExtra<T>();
  }
  
  protected final String name_;
  
  private Extra() {
    name_ = PACKAGE + UUID.randomUUID();
  }
  
  public abstract T get(Intent i);
  
  public abstract void put(Intent i, T value);
}