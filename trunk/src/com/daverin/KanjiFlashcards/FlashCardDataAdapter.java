package com.daverin.KanjiFlashcards;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The FlashCardDataAdapter class encapsulates all database interactions.
 * It also handles versioning of the data.  The version is stored with each
 * stored configuration, so if a version is found that is not current, it
 * will be dropped.
 * 
 @author: Joe Daverin (joe@daverin.com)
 */
public class FlashCardDataAdapter {

  // The database version number tells the application when to invalidate
  // old saved data.  When this number increases everyone loses their
  // saved configuration.
  private static final int DATABASE_VERSION = 1;

  // This is the name of the key field for the database.  Since there is
  // only ever one entry, it's pretty useless.
  public static final String KEY_CONFIGURATION_ID = "_id";

  // This is the name of the value field for the database.  It should
  // contain a serialized version of a deck configuration, which is a list
  // of BitSet objects.
  public static final String KEY_DECK_CONFIGURATION = "deck_config";

  // This database name and table name are what Android needs to identify
  // this data store.
  private static final String ANDROID_DATABASE_NAME = "kanji_flashcards";
  private static final String ANDROID_DATA_STORE = "configuration";

  // This tag serves as an identifier for logging.
  private static final String TAG = "FlashCardDataAdapter";

  // These are for convenience.  The helper makes connecting to a database
  // a little easier, and it can keep around the opened database in "db".
  private FlashCardDatabaseHelper flashcardDBHelper;
  private SQLiteDatabase db;

  // The database needs to check that its data has been initialized, but
  // we don't want to open databases just to check, so this will check on
  // the first access to an opened database, then remember so it doesn't
  // check again.
  private boolean configuration_table_initialized_;

  /**
   * This inner class primarily creates tables if they don't exist and
   * handles version number checking when the database schema is changed.
   */
  private static class FlashCardDatabaseHelper extends SQLiteOpenHelper {
    FlashCardDatabaseHelper(Context context) {
      super(context, ANDROID_DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This will create a new data store using the preset names and fields.
    @Override
    public void onCreate(SQLiteDatabase db) {
      Log.w(TAG, "Creating database from scratch.");
      db.execSQL("create table " + ANDROID_DATA_STORE + " (" +
          KEY_CONFIGURATION_ID + " integer primary key autoincrement, " +
          KEY_DECK_CONFIGURATION + " text not null);");
      }

    // This will upgrade the database, which in this case means destroying
    // the old database and creating a new one from scratch.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion +
          " to " + newVersion + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + ANDROID_DATA_STORE);
      onCreate(db);
    }
  }

  // When a database adapter is first created the data store hasn't been
  // checked for initialization yet.
  public FlashCardDataAdapter(Context ctx) {
    flashcardDBHelper = new FlashCardDatabaseHelper(ctx);
    configuration_table_initialized_ = false;
  }

  // Opening the database populates the "db" object with a database pointer
  // we can interact with.
  public FlashCardDataAdapter open() throws SQLException {
   db = flashcardDBHelper.getWritableDatabase();
   return this;
  }

  // Closing the database closes all open database connections that the
  // database helper has opened.
  public void close() {
    flashcardDBHelper.close();
  }

  // Whenever data is accessed, there is a check that the storage has been
  // initialized.  This takes the form of a query to the database, and if
  // the expected data isn't found, a blank value is placed into the right
  // spot in the database.  This should only happen once, there is a check
  // that will shortcut evaluation if this has been run before.
  public void initializeDeckConfiguration() {
   if (configuration_table_initialized_) return;
   configuration_table_initialized_ = true;
  
   // Try to get the deck configuration value from the data store.  If one is
   // found, we can just return, there's nothing more to do.
   Cursor cursor = db.query(true, ANDROID_DATA_STORE,
       new String[] {KEY_DECK_CONFIGURATION},
       null, null, null, null, null, null);
   if (cursor != null) {
     if (cursor.getCount() > 0) {
       return;
     }
   }
  
   // Clear out the data store, just in case something weird crept in.
   db.delete(ANDROID_DATA_STORE, null, null);
  
   // If the deck configuration wasn't found in the data store, then create a
   // new one and add it to the data store.
   ContentValues initial_deck_configuration = new ContentValues();
   initial_deck_configuration.put(KEY_DECK_CONFIGURATION, "");
   db.insert(ANDROID_DATA_STORE, null, initial_deck_configuration);
  }

  // Retrieve a saved deck configuration from the data store.
  public List<BitSet> getDeckConfiguration(List<BitSet> starting_configuration) {
    ArrayList<BitSet> new_deck_configuration =
        new ArrayList<BitSet>(starting_configuration);
    String serialized_deck_configuration = getDeckConfigurationString();
    String[] deck_configurations = serialized_deck_configuration.split(":");
    for (int deck = 0; deck < deck_configurations.length &&
        deck < new_deck_configuration.size(); ++deck) {
      for (int subset = 0; subset < deck_configurations[deck].length() &&
          subset < new_deck_configuration.get(deck).size(); ++subset) {
        new_deck_configuration.get(deck).set(subset,
            deck_configurations[deck].charAt(subset) == '1');
      }
    }
    return new_deck_configuration;
  }

  // Save a deck configuration to the data store.
  public void saveDeckConfiguration(List<BitSet> deck_configuration) {
    String serialized_deck_configuration = "";
    for (int i = 0; i < deck_configuration.size(); ++i) {
      for (int j = 0; j < deck_configuration.get(i).size(); ++j) {
        serialized_deck_configuration +=
            deck_configuration.get(i).get(j) ? '1' : '0';
      }
      serialized_deck_configuration += ':';
    }
    setDeckConfigurationString(serialized_deck_configuration);
  }

  // Retrieve the saved deck configuration from the data store.
  public String getDeckConfigurationString() {
    initializeDeckConfiguration();
    Cursor cursor = db.query(true, ANDROID_DATA_STORE,
        new String[] {KEY_DECK_CONFIGURATION}, null, null, null, null,
        null, null);
    if (cursor != null) {
      if (cursor.getCount() > 0) {
        cursor.moveToLast();
        return cursor.getString(0);
      }
    }
    return "";
  }

  // Set the value of the deck configuration in the data store.
  public void setDeckConfigurationString(
      String serialized_deck_configuration) {
    initializeDeckConfiguration();
    ContentValues args = new ContentValues();
    args.put(KEY_DECK_CONFIGURATION, serialized_deck_configuration);
    db.update(ANDROID_DATA_STORE, args, null, null);
  }
}
