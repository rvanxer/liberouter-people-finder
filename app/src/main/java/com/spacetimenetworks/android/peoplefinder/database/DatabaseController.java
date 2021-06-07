/*
    Liberouter People Finder
    Copyright (C) 2021 Teemu Kärkkäinen

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as
    published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.spacetimenetworks.android.peoplefinder.database;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class encapsulates an SQLite database and provides a convenient way to
 * interact one in asynchronous manner. Follows the actor model by using a
 * single worker thread and a queue where all API calls are inserted into.
 *
 * @author teemuk
 */
public class DatabaseController
    extends Service {

  /** Tag for log lines */
  private final String TAG = this.getClass().getSimpleName();




  //=========================================================================//
  // Instance vars
  //=========================================================================//
  /** Binder for activities */
  private final IBinder binder
      = new DatabaseController.DatabaseBinder();
  /** Task executor */
  private ExecutorService executor;
  /** The database encapsulated by this controller */
  private SQLiteDatabase database;

  /** Callbacks for database person insertions. */
  private final Collection<OnInsertedPerson> onInsertedPersonCallbacks
      = new CopyOnWriteArraySet<>();

  /** Callbacks for database note insertions. */
  private final Collection<OnInsertedNote> onInsertedNoteCallbacks
      = new CopyOnWriteArraySet<>();
  //=========================================================================//


  //=========================================================================//
  // API
  //-------------------------------------------------------------------------//
  // Public API for the service. Since database operations can take
  // arbitrary time to execute, the API is asynchronous. All commands are
  // executed in FIFO order by the controller thread,
  // which also invokes any appropriate callbacks.
  //=========================================================================//

  /**
   * Queries the database for entries for a particular name. The search uses
   * {@code full_name LIKE %name%} type query.
   *
   * @param name
   *     name the search for
   * @param callback
   *     callback invoked after the query has been executed
   */
  public void findPersons( final String name,
                           final FindPersonsQueryFinished callback ) {
    // Build query
    final String query = DatabaseModel.SQL_SELECT_PERSONS.replace( "[NAME]", name );

    // Submit the query
    this.executor.submit(
        new QueryTask(
            // Query to execute
            query,
            // Callback invoked after execution
            cursor -> {
              final List<DataModel.PersonName> results
                  = personNameCursorToList( cursor );
              cursor.close();
              callback.queryFinished( results );
            }
        )
    );
  }

  /**
   * Query the database for all persons.
   *
   * @param callback
   *     the callback invoked with the results
   */
  public void findAllPersons( final FindPersonsQueryFinished callback ) {
    // Build query
    final String query = DatabaseModel.SQL_SELECT_ALL_PERSONS_WITH_STATUS;

    // Submit the query
    this.executor.submit(
        new QueryTask(
            // Query to execute
            query,
            // Callback invoked after execution
            cursor -> {
              final List<DataModel.PersonName> results
                  = personNameCursorToList( cursor );
              cursor.close();
              callback.queryFinished( results );
            }
        )
    );
  }

  /**
   * Queries the database for the detail record of a given person.
   *
   * @param recordID
   *     record ID
   * @param callback
   *     callback invoked after the query has been executed
   */
  public void getPersonDetails( final String recordID,
                                final GetPersonDetailsQueryFinished callback
  ) {
    // Build query
    final String query = DatabaseModel.SQL_SELECT_PERSON_DETAILS.replace( "[PERSON_ID]",
        recordID );

    // Submit query
    this.executor.submit(
        new QueryTask(
            // Query to execute
            query,
            // Callback invoked after execution
            cursor -> {
              final DataModel.LocalPerson record =
                  personRecordFromCursor( cursor );
              cursor.close();
              callback.queryFinished( record );
            }
        )
    );
  }

  public void getNotesForPerson( final String personID,
                                 final GetNotesQueryFinished callback ) {
    // Build query
    final String query = DatabaseModel.SQL_SELECT_NOTES_FOR_PERSON.replace( "[PERSON_ID]",
        personID );

    // Submit query
    this.executor.submit(
        new QueryTask(
            // Query to execute
            query,
            // Callback invoked after execution
            cursor -> {
              final List<DataModel.LocalNote> notes =
                  notesCursorToList( cursor );
              cursor.close();
              callback.queryFinished( notes );
            }
        )
    );
  }

  /**
   * Inserts a person into the database
   *
   * @param person
   *     person to insert
   * @param callback
   *     callback to be invoked after insertion
   */
  public void insertPerson( final DataModel.LocalPerson person,
                            final OnInsertedPerson callback ) {
    // Precondition check
    if ( person.person.metadata.recordID == null ||
         person.person.metadata.recordID.length() == 0 ) {
      throw new IllegalArgumentException( "Person record -> metadata -> " +
                                          "record ID cannot be null or " +
                                          "empty." );
    }

    // Submit a new insertion task
    this.executor.submit( new PersonInsertTask( person, callback ) );
  }

  /**
   * Inserts a note into the database. Note must have record ID and person ID
   * set.
   *
   * @param note
   *     note to insert
   * @param callback
   *     callback invoked after insertion
   */
  public void insertNote( final DataModel.LocalNote note,
                          final OnInsertedNote callback ) {
    // Precondition Check
    if ( note.note.metadata.recordID == null ||
         note.note.metadata.recordID.length() == 0 ) {
      throw new IllegalArgumentException( "Note record -> metadata -> " +
                                          "record ID cannot be null or " +
                                          "empty." );
    }
    if ( note.note.metadata.personID == null ||
         note.note.metadata.personID.length() == 0 ) {
      throw new IllegalArgumentException( "Note record -> metadata -> " +
                                          "person ID cannot be null or " +
                                          "empty." );
    }

    // Submit a new insertion task
    this.executor.submit( new NoteInsertTask( note, callback ) );
  }

  /**
   * Callback invoked every time a new person is inserted into the database.
   *
   * @param callback
   *     callback to invoke
   */
  public void addOnInsertedPersonCallback( OnInsertedPerson callback ) {
    this.onInsertedPersonCallbacks.add( callback );
  }

  /**
   * Remove a previously added callback.
   *
   * @param callback
   *     callback to remove
   */
  public void removeOnInsertedPersonCallback( OnInsertedPerson callback ) {
    this.onInsertedPersonCallbacks.remove( callback );
  }

  /**
   * Callback invoked every time a new note is inserted into the database.
   *
   * @param callback
   *     callback to invoke
   */
  public void addOnInsertedNoteCallback( OnInsertedNote callback ) {
    this.onInsertedNoteCallbacks.add( callback );
  }

  /**
   * Remove a previously added callback.
   *
   * @param callback
   *     callback to remove
   */
  public void removeOnInsertedNoteCallback( OnInsertedNote callback ) {
    this.onInsertedNoteCallbacks.remove( callback );
  }

  /**
   * Sets the routed status of the given message.
   *
   * @param personID
   *     person record ID whose status to set
   * @param callback
   *     callback to invoke once the query is finished
   */
  public void setPersonRouted( String personID, ExecuteFinished callback ) {
    String query = DatabaseModel.SQL_UPDATE_PERSON_ROUTED.replace( "[PERSON_ID]",
        personID );
    this.executor.submit( new ExecuteForCallbackTask( query, callback ) );
  }

  /**
   * Sets the routed status of the given note.
   *
   * @param noteID
   *     person record ID whose status to set
   * @param callback
   *     callback to invoke once the query is finished
   */
  public void setNoteRouted( String noteID, ExecuteFinished callback ) {
    String query = DatabaseModel.SQL_UPDATE_NOTE_ROUTED.replace( "[NOTE_ID]",
        noteID );
    this.executor.submit( new ExecuteForCallbackTask( query, callback ) );
  }

  /** Marks the person and all attached notes as routed. */
  public void setRouted( DataModel.SerializablePerson person ) {
    // TODO: Does this need a callback?
    this.executor.submit( new SetRoutedTask( person ) );
  }

  /**
   * Returns all unrouted person records.
   *
   * @param callback
   *     callback to invoke after the query finishes
   */
  public void getUnroutedPersons( final FindPersonsQueryFinished callback ) {
    this.executor.submit(
        new QueryTask(
            // Query to execute
            DatabaseModel.SQL_SELECT_UNROUTED_PERSONS,
            // Callback invoked after execution
            cursor -> {
              final List<DataModel.PersonName> results
                  = personNameCursorToList( cursor );
              cursor.close();
              callback.queryFinished( results );
            }
        )
    );
  }

  /**
   * Returns all unrouted note records.
   *
   * @param callback
   *     callback to invoke after the query finishes
   */
  public void getUnroutedNotes( final GetNotesQueryFinished callback ) {
    this.executor.submit(
        new QueryTask(
            // Query to execute
            DatabaseModel.SQL_SELECT_UNROUTED_NOTES,
            // Callback invoked after execution
            cursor -> {
              final List<DataModel.LocalNote> results
                  = notesCursorToList( cursor );
              cursor.close();
              callback.queryFinished( results );
            }
        )
    );
  }

  /**
   * Builds a list of all persons with unrouted records. The list will contain
   * ALL notes for each person that has either unrouted person record or any
   * number of unrouted note records.
   *
   * @param callback
   *     callback to invoke with the results
   */
  public void getAllUnrouted( final GetAllUnroutedQueryFinished callback ) {
    if ( callback != null ) {
      this.executor.submit( new GetAllUnroutedTask( callback ) );
    }
  }

  //-------------------------------------------------------------------------//
  // Callback definitions
  //-------------------------------------------------------------------------//

  /** Interface for callbacks invoked after {@link #findPersons} finishes. */
  public interface FindPersonsQueryFinished {
    /**
     * The query has been executed.
     *
     * @param names
     *     names of the persons matching the query
     */
    void queryFinished( List<DataModel.PersonName> names );
  }

  /**
   * Interface for callbacks invoked after {@link #getPersonDetails} finishes.
   */
  public interface GetPersonDetailsQueryFinished {
    void queryFinished( DataModel.LocalPerson person );
  }

  /**
   * Interface for callbacks invoked after {@link #getNotesForPerson} finishes.
   */
  public interface GetNotesQueryFinished {
    /**
     * List of notes attached to the person.
     *
     * @param notes
     *     notes for the person
     */
    void queryFinished( List<DataModel.LocalNote> notes );
  }

  /**
   * Interface for callbacks invoked after {@link #getAllUnrouted}.
   */
  public interface GetAllUnroutedQueryFinished {
    /**
     * Query has finished.
     *
     * @param unrouted
     *     list of serializable entities that contains all unrouted records
     *     (both persons and notes) in the database
     * @param picPaths
     *     maps person record ID to a path of a photo in the local filesyste, if
     *     one exists
     */
    void queryFinished( Collection<DataModel.SerializablePerson> unrouted,
                               Map<String, String> picPaths );
  }

  /**
   * Interface for receiving a callback when a person is inserted into the
   * database.
   */
  public interface OnInsertedPerson {
    /**
     * A row insertion operation has finished. The operation might have
     * succeeded or failed, which can be determined from the ID.
     *
     * @param id
     *     id of the inserted row, or -1 if insertion failed
     * @param person
     *     person record that was inserted
     */
    void onInserted( long id, DataModel.LocalPerson person );
  }

  /**
   * Interface for receiving a callback when a note is inserted into the
   * database.
   */
  public interface OnInsertedNote {
    /**
     * A row insertion operation has finished. The operation might have
     * succeeded or failed, which can be determined from the ID.
     *
     * @param id
     *     id of the inserted row, or -1 if insertion failed
     * @param note
     *     note that was inserted
     */
    void onInserted( long id, DataModel.LocalNote note );
  }

  /** Interface for callbacks when query has finished. */
  public interface QueryFinished {
    /**
     * A query has finished.
     *
     * @param cursor
     *     Results of the query if the query had results.
     */
    void queryFinished( Cursor cursor );
  }

  /** Interface for callbacks when an execute operation finishes */
  public interface ExecuteFinished {
    void executeFinished();
  }



  //=========================================================================//
  // Service Lifecycle
  //=========================================================================//
  @Override
  public IBinder onBind( Intent intent ) {
    Log.d( TAG, "onBind()" );

    return this.binder;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // Create the executor
    this.executor = Executors.newSingleThreadExecutor();

    // Initialize the database
    this.executor.submit( new InitDatabaseTask() );

    Log.d( TAG, "onCreate()" );
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Shut down the executor
    this.executor.shutdownNow();

    // Close the database
    if ( this.database != null ) {
      this.database.close();
    }

    Log.d( TAG, "onDestroy()" );
  }

  @Override
  public void onRebind( Intent intent ) {
    super.onRebind( intent );

    Log.d( TAG, "onRebind()" );
  }

  @Override
  public int onStartCommand( Intent intent, int flags, int startId ) {
    super.onStartCommand( intent, flags, startId );

    Log.d( TAG, "onStartCommand()" );

    return START_STICKY;
  }
  //=========================================================================//


  //=========================================================================//
  // Private
  //=========================================================================//

  /**
   * Reads the data from the cursor starting from the beginning and creates a
   * list from the entries.
   * <p/>
   * Assumes first column is a record id as a string and second the full
   * name as
   * a string.
   * <p/>
   * Has no side effects on the cursor after returning (position won't be
   * changed and cursor won't be closed).
   *
   * @param cursor
   *     the cursor to read from
   *
   * @return list of entries from the cursor
   */
  private List<DataModel.PersonName> personNameCursorToList( final Cursor cursor ) {
    // Create the list
    int listSize = ( cursor == null ) ? ( 0 ) : ( cursor.getCount() );
    List<DataModel.PersonName> items = new ArrayList<>( listSize );

    // Add the items from the cursor to the list
    if ( cursor != null ) {
      int start = cursor.getPosition();
      cursor.moveToPosition( -1 );
      while ( cursor.moveToNext() ) {
        // Pull out the fields
        String id = cursor.getString( 0 );
        String fullName = cursor.getString( 1 );
        String status = cursor.getString( 2 );
        Long entryDate = cursor.getLong( 3 );

        // Create a new item
        DataModel.PersonName item = new DataModel.PersonName( id, fullName, status, entryDate );
        items.add( item );
      }
      cursor.moveToPosition( start );
    }

    return items;
  }

  /**
   * Reads the data from the cursor starting from the beginning and creates a
   * list from the entries.
   * <p/>
   * Has no side effects on the cursor after returning (position won't be
   * changed and cursor won't be closed).
   *
   * @param cursor
   *     the cursor to read from
   *
   * @return list of entries from the cursor
   */
  private List<DataModel.LocalNote> notesCursorToList( final Cursor cursor ) {
    // Create the list
    int listSize = ( cursor == null ) ? ( 0 ) : ( cursor.getCount() );
    List<DataModel.LocalNote> items = new ArrayList<>( listSize );

    // Add the items from the cursor to the list
    if ( cursor != null ) {
      int start = cursor.getPosition();
      cursor.moveToPosition( -1 );
      while ( cursor.moveToNext() ) {
        // Pull out the fields
        String recordID = cursor.getString( 0 );
        String personID = cursor.getString( 1 );
        String linkedPersonID = cursor.getString( 2 );
        Long entryDate = cursor.getLong( 3 );
        String authorName = cursor.getString( 4 );
        String authorEmail = cursor.getString( 5 );
        String authorPhone = cursor.getString( 6 );
        Long sourceDate = cursor.getLong( 7 );
        String contact = cursor.getString( 8 );
        String status = cursor.getString( 9 );
        String personEmail = cursor.getString( 10 );
        String personPhone = cursor.getString( 11 );
        String lastLocation = cursor.getString( 12 );
        String text = cursor.getString( 13 );
        String photoUrl = cursor.getString( 14 );
        String photoPath = cursor.getString( 15 );
        Boolean routed = ( cursor.getLong( 16 ) == 1 );

        boolean madeContact = contact.equalsIgnoreCase( "true" );

        // Create a new item
        DataModel.Note note = new DataModel.Note( recordID, personID, linkedPersonID,
            entryDate, authorName, authorEmail,
            authorPhone, sourceDate, madeContact, status,
            personEmail, personPhone, lastLocation,
            text, photoUrl );
        DataModel.LocalNote item = new DataModel.LocalNote( note, photoPath, routed );
        items.add( item );
      }
      cursor.moveToPosition( start );
    }

    return items;
  }

  /**
   * Reads a person record from the cursor. Th cursor should contain one row
   * with all the columns of the PERSON table. Leaves no side effects on the
   * cursor after returning.
   *
   * @param cursor
   *     the cursor to read from
   *
   * @return person record with the cursor data or {@code null}
   */
  private DataModel.LocalPerson personRecordFromCursor( final Cursor cursor ) {
    DataModel.LocalPerson record = null;

    if ( cursor != null ) {
      int start = cursor.getPosition();
      cursor.moveToPosition( -1 );
      if ( cursor.moveToNext() ) {
        DataModel.Person person = new DataModel.Person(
            cursor.getString( 0 ),  // record ID
            cursor.getLong( 1 ),  // entry date
            cursor.getLong( 2 ),  // expiry date
            cursor.getString( 3 ),  // author name
            cursor.getString( 4 ),  // author email
            cursor.getString( 5 ),  // author phone
            cursor.getString( 6 ),  // source name
            cursor.getLong( 7 ),  // source date
            cursor.getString( 8 ),  // source URL
            cursor.getString( 9 ),  // full name
            cursor.getString( 10 ), // given name
            cursor.getString( 11 ), // family name
            cursor.getString( 12 ), // alternate names
            cursor.getString( 13 ), // description
            cursor.getString( 14 ), // sex
            cursor.getString( 15 ), // date of birth
            cursor.getLong( 16 ), // age
            cursor.getString( 17 ), // home street
            cursor.getString( 18 ), // home neighborhood
            cursor.getString( 19 ), // home city
            cursor.getString( 20 ), // home home state
            cursor.getString( 21 ), // home zip
            cursor.getString( 22 ), // home country
            cursor.getString( 23 ), // photo url
            cursor.getString( 24 )  // profile urls
        );

        record = new DataModel.LocalPerson(
            person,
            cursor.getString( 25 ),
            // photo path
            cursor.getLong( 26 ) == 1 // routed
        );

      }
      cursor.moveToPosition( start );
    }

    return record;
  }

  //=========================================================================//


  //=========================================================================//
  // Tasks
  //-------------------------------------------------------------------------//
  // Executor tasks for the controller
  //=========================================================================//

  /** Queries the person table for matching entries */
  private class QueryTask
      implements Runnable {
    private final String query;
    private final QueryFinished callback;

    public QueryTask( String query, QueryFinished callback ) {
      this.query = query;
      this.callback = callback;
    }

    @Override
    public void run() {
      // Run the query
      SQLiteDatabase db = DatabaseController.this.database;
      Cursor results = null;
      if ( db != null ) {
        results = db.rawQuery( this.query, null );
      } else {
        Log.e( TAG, "No database found. Cannot run query." );
      }

      // Invoke the callback
      if ( this.callback != null ) {
        this.callback.queryFinished( results );
      }
    }
  }

  /** Attempts to insert a person entry into the database. */
  private class PersonInsertTask
      implements Runnable {
    private final DataModel.LocalPerson person;
    private final OnInsertedPerson onInserted;

    public PersonInsertTask( DataModel.LocalPerson person,
                             OnInsertedPerson onInserted ) {
      this.person = person;
      this.onInserted = onInserted;
    }

    @Override
    public void run() {
      // Content values to insert
      ContentValues values = new ContentValues( 27 );
      values.put( DatabaseModel.PERSON_COL_RECORD_ID,
          this.person.person.metadata.recordID );
      values.put( DatabaseModel.PERSON_COL_ENTRY_DATE,
          this.person.person.metadata.entryDate );
      values.put( DatabaseModel.PERSON_COL_EXPIRY_DATE,
          this.person.person.metadata.expiryDate );
      values.put( DatabaseModel.PERSON_COL_AUTHOR_NAME,
          this.person.person.metadata.authorName );
      values.put( DatabaseModel.PERSON_COL_AUTHOR_EMAIL,
          this.person.person.metadata.authorEmail );
      values.put( DatabaseModel.PERSON_COL_AUTHOR_PHONE,
          this.person.person.metadata.authorPhone );
      values.put( DatabaseModel.PERSON_COL_SOURCE_NAME,
          this.person.person.metadata.sourceName );
      values.put( DatabaseModel.PERSON_COL_SOURCE_DATE,
          this.person.person.metadata.sourceDate );
      values.put( DatabaseModel.PERSON_COL_SOURCE_URL,
          this.person.person.metadata.sourceUrl );
      values.put( DatabaseModel.PERSON_COL_FULL_NAME,
          this.person.person.identity.name.fullName );
      values.put( DatabaseModel.PERSON_COL_GIVEN_NAME,
          this.person.person.identity.name.givenName );
      values.put( DatabaseModel.PERSON_COL_FAMILY_NAME,
          this.person.person.identity.name.familyName );
      values.put( DatabaseModel.PERSON_COL_ALT_NAMES,
          this.person.person.identity.name.alternateNames );
      values.put( DatabaseModel.PERSON_COL_DESCRIPTION,
          this.person.person.identity.description );
      values.put( DatabaseModel.PERSON_COL_SEX, this.person.person.identity.sex );
      values.put( DatabaseModel.PERSON_COL_DOB, this.person.person.identity.dateOfBirth );
      values.put( DatabaseModel.PERSON_COL_AGE, this.person.person.identity.age );
      values.put( DatabaseModel.PERSON_COL_HOME_STREET,
          this.person.person.identity.home.street );
      values.put( DatabaseModel.PERSON_COL_HOME_NGHBRHD,
          this.person.person.identity.home.neighborhood );
      values.put( DatabaseModel.PERSON_COL_HOME_CITY,
          this.person.person.identity.home.city );
      values.put( DatabaseModel.PERSON_COL_HOME_STATE,
          this.person.person.identity.home.state );
      values.put( DatabaseModel.PERSON_COL_HOME_ZIP,
          this.person.person.identity.home.zip );
      values.put( DatabaseModel.PERSON_COL_HOME_COUNTRY,
          this.person.person.identity.home.country );
      values.put( DatabaseModel.PERSON_COL_PHOTO_URL,
          this.person.person.identity.photoUrl );
      values.put( DatabaseModel.PERSON_COL_PHOTO_PATH, this.person.photoPath );
      values.put( DatabaseModel.PERSON_COL_PROFILE_URLS,
          this.person.person.identity.profileUrls );
      values.put( DatabaseModel.PERSON_COL_ROUTED,
          ( this.person.routed ) ? ( 1 ) : ( 0 ) );

      // Try to insert
      long result;
      result = database.insert( DatabaseModel.PERSON_TABLE_NAME, null, values );

      // Invoke callback
      if ( this.onInserted != null ) {
        this.onInserted.onInserted( result, this.person );
      }

      // Invoke other callbacks
      for ( OnInsertedPerson cb : onInsertedPersonCallbacks ) {
        cb.onInserted( result, this.person );
      }
    }
  }

  /** Attempts to insert a note entry into the database. */
  private class NoteInsertTask
      implements Runnable {
    private final DataModel.LocalNote note;
    private final OnInsertedNote onInserted;

    public NoteInsertTask( DataModel.LocalNote note,
                           OnInsertedNote onInserted ) {
      this.note = note;
      this.onInserted = onInserted;
    }

    @Override
    public void run() {
      // Content values to insert
      ContentValues values = new ContentValues( 17 );
      values.put( DatabaseModel.NOTE_COL_RECORD_ID, this.note.note.metadata.recordID );
      values.put( DatabaseModel.NOTE_COL_PERSON_ID, this.note.note.metadata.personID );
      values.put( DatabaseModel.NOTE_COL_LNK_PERSON_ID,
          this.note.note.metadata.linkedPersonID );
      values.put( DatabaseModel.NOTE_COL_ENTRY_DATE, this.note.note.metadata.entryDate );
      values.put( DatabaseModel.NOTE_COL_AUTHOR_NAME, this.note.note.metadata.authorName );
      values.put( DatabaseModel.NOTE_COL_AUTHOR_EMAIL,
          this.note.note.metadata.authorEmail );
      values.put( DatabaseModel.NOTE_COL_AUTHOR_PHONE,
          this.note.note.metadata.authorPhone );
      values.put( DatabaseModel.NOTE_COL_SOURCE_DATE, this.note.note.metadata.sourceDate );
      values.put( DatabaseModel.NOTE_COL_CONTACT,
          ( this.note.note.status.authorMadeContact != null &&
            this.note.note.status.authorMadeContact ) ?
              ( "true" ) : ( "false" )
      );
      values.put( DatabaseModel.NOTE_COL_STATUS, this.note.note.status.status );
      values.put( DatabaseModel.NOTE_COL_PERSON_EMAIL,
          this.note.note.status.emailOfFoundPerson );
      values.put( DatabaseModel.NOTE_COL_PERSON_PHONE,
          this.note.note.status.phoneOfFoundPerson );
      values.put( DatabaseModel.NOTE_COL_LAST_LOCATION,
          this.note.note.status.lastKnownLocation );
      values.put( DatabaseModel.NOTE_COL_TEXT, this.note.note.status.text );
      values.put( DatabaseModel.NOTE_COL_PHOTO_URL, this.note.note.status.photoUrl );
      values.put( DatabaseModel.NOTE_COL_PHOTO_PATH, this.note.photoPath );
      values.put( DatabaseModel.NOTE_COL_ROUTED, ( this.note.routed ) ?
          ( 1 ) : ( 0 ) );


      // Try to insert
      long result;
      result = database.insert( DatabaseModel.NOTE_TABLE_NAME, null, values );

      // Invoke callback
      if ( this.onInserted != null ) {
        this.onInserted.onInserted( result, this.note );
      }

      // Invoke other callbacks
      for ( OnInsertedNote cb : onInsertedNoteCallbacks ) {
        cb.onInserted( result, this.note );
      }
    }
  }

  /** Runs an execSQL command and invokes the callback. */
  private class ExecuteForCallbackTask
      implements Runnable {
    private final ExecuteFinished callback;
    private final String query;

    public ExecuteForCallbackTask( String query, ExecuteFinished callback ) {
      this.callback = callback;
      this.query = query;
    }

    @Override
    public void run() {
      // Execute query
      database.execSQL( this.query );

      // Callback
      if ( this.callback != null ) {
        this.callback.executeFinished();
      }
    }
  }

  /**
   * Task that builds a list of all unrouted records. The list will contain the
   * person record and ALL note records attached to that person for each person
   * that has either unrouted person record or any number of unrouted note
   * records.
   * <p/>
   * The idea is to build the content needed for sending out the unrouted data.
   * We want to send out all the data we have for each person, not just the
   * unrouted bits.
   */
  private class GetAllUnroutedTask
      implements Runnable {
    private final GetAllUnroutedQueryFinished callback;

    public GetAllUnroutedTask( GetAllUnroutedQueryFinished callback ) {
      this.callback = callback;
    }

    @Override
    public void run() {
      // Get database reference
      SQLiteDatabase db = DatabaseController.this.database;
      if ( db == null ) {
        Log.d( TAG, "No database found. Cannot run query." );

        // Make sure the callback gets invoked
        if ( this.callback != null ) {
          this.callback.queryFinished(
              new ArrayList<>( 0 ),
              new HashMap<>( 0 )
          );
        }
        return;
      }

      // Structures for results
      Collection<DataModel.SerializablePerson> results
          = new ArrayList<>( 0 );
      // Map for tracking any paths to photos in the local file system
      Map<String, String> picPaths = new LinkedHashMap<>();


      // We do everything in a single transaction so that the database is
      // not modified while we build the result.
      try {
        db.beginTransaction();

        // Build using a Map, PersonID -> SerializablePerson
        Map<String, DataModel.SerializablePerson> resultMap
            = new LinkedHashMap<>();

        // First get all unrouted Person records
        Cursor cursor = db.rawQuery( DatabaseModel.SQL_SELECT_UNROUTED_PERSONS, null );
        List<DataModel.PersonName> personNames = personNameCursorToList( cursor );
        cursor.close();

        // Create records for all unrouted persons
        for ( DataModel.PersonName person : personNames ) {
          if ( !resultMap.containsKey( person.recordID ) ) {
            // Get the details for the unrouted person
            this.addPersonDetails( person.recordID, resultMap,
                picPaths, db );
          }
        }

        // Get all unrouted notes
        cursor = db.rawQuery( DatabaseModel.SQL_SELECT_UNROUTED_NOTES, null );
        List<DataModel.LocalNote> notes = notesCursorToList( cursor );
        cursor.close();

        // Add persons of all unrouted notes to the results
        for ( DataModel.LocalNote note : notes ) {
          if ( !resultMap.containsKey( note.note.metadata.personID ) ) {
            // Get the details for the unrouted person
            this.addPersonDetails( note.note.metadata.personID,
                resultMap, picPaths, db );
          }
        }

        // Result map now contains records for all persons that have
        // either unrouted person record or unrouted notes.

        // Include ALL notes for each person (not just unrouted). This
        // means that the messages that we send based on this data include
        // the full record of each person.
        for ( Map.Entry<String, DataModel.SerializablePerson> e :
            resultMap.entrySet() ) {
          this.addAllNotes( e.getKey(), e.getValue().notes, db );
        }

        // Now we have a ready list
        results = resultMap.values();

        db.setTransactionSuccessful();
      } catch ( Exception e ) {
        Log.d( TAG, "GetAllUnroutedTask.run() failed: " + e.getMessage() );
      } finally {
        db.endTransaction();
      }

      // Invoke the callback
      if ( this.callback != null ) {
        this.callback.queryFinished( results, picPaths );
      }
    }

    private void addPersonDetails( String recordID,
                                   Map<String, DataModel.SerializablePerson> resultMap,
                                   Map<String, String> picPaths,
                                   SQLiteDatabase db ) {
      Cursor cursor = db.rawQuery(
          DatabaseModel.SQL_SELECT_PERSON_DETAILS
              .replace( "[PERSON_ID]", recordID ),
          null );
      DataModel.LocalPerson record = personRecordFromCursor( cursor );
      cursor.close();

      // Add the result record
      DataModel.SerializablePerson resultRecord =
          new DataModel.SerializablePerson( record.person,
              new LinkedList<>() );
      resultMap.put( recordID, resultRecord );

      // Add picture path if necessary
      if ( record.photoPath != null && record.photoPath.length() > 0 ) {
        picPaths.put( recordID, record.photoPath );
      }
    }

    private void addAllNotes( String personID, List<DataModel.Note> notes,
                              SQLiteDatabase db ) {
      Cursor cursor = db.rawQuery(
          DatabaseModel.SQL_SELECT_NOTES_FOR_PERSON.replace( "[PERSON_ID]", personID ),
          null
      );
      List<DataModel.LocalNote> foundNotes = notesCursorToList( cursor );
      cursor.close();

      for ( DataModel.LocalNote note : foundNotes ) {
        notes.add( note.note );
      }
    }
  }

  private class SetRoutedTask
      implements Runnable {
    private final DataModel.SerializablePerson person;

    public SetRoutedTask( DataModel.SerializablePerson person ) {
      this.person = person;
    }

    @Override
    public void run() {
      // Get database reference
      SQLiteDatabase db = DatabaseController.this.database;
      if ( db == null ) {
        Log.d( TAG, "No database found. Cannot set routed status for " +
                    "SerializablePerson '" +
                    person.person.metadata.recordID + "'." );
        return;
      }

      // Do the whole thing as a transaction
      try {
        db.beginTransaction();

        // Set the person as routed
        this.setPersonRouted( person.person.metadata.recordID, db );

        // Loop through the notes and set routed for all.
        // (Some of them might already have routed status set,
        // but we would have to query the database to find out since the
        // serializable form doesn't contain local state like the routed
        // status)
        for ( DataModel.Note note : person.notes ) {
          this.setNoteRouted( note.metadata.recordID, db );
        }

        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }

    private void setPersonRouted( String personID, SQLiteDatabase db ) {
      String query = DatabaseModel.SQL_UPDATE_PERSON_ROUTED.replace( "[PERSON_ID]",
          personID );
      db.execSQL( query );
    }

    private void setNoteRouted( String noteID, SQLiteDatabase db ) {
      String query = DatabaseModel.SQL_UPDATE_NOTE_ROUTED.replace( "[NOTE_ID]",
          noteID );
      db.execSQL( query );
    }
  }

  /** Initializes the database using a {@code DbHelper}. */
  private class InitDatabaseTask
      implements Runnable {
    @Override
    public void run() {
      // Get an SQLite database instance using a helper,
      // which takes care of creating and upgrading the database.
      // XXX: Can the helper be abandoned without closing since
      // database.close() is called by the service?
      DbHelper helper = new DbHelper( DatabaseController.this );
      SQLiteDatabase database = helper.getWritableDatabase();

      // Store the reference
      if ( database != null ) {
        DatabaseController.this.database = database;
      } else {
        Log.e( TAG, "Failed to get database instance." );
      }
    }
  }
  //=========================================================================//


  //=========================================================================//
  // Database helper
  //-------------------------------------------------------------------------//
  // Code for managing the database versioning by using a database helper
  //=========================================================================//
  // TODO:
  // - Add migration between schemas if the schema changes after release
  private static class DbHelper
      extends SQLiteOpenHelper {

    public DbHelper( Context context ) {
      super( context, DatabaseModel.DB_NAME, null, DatabaseModel.DB_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
      // Generate the tables
      db.execSQL( DatabaseModel.SQL_CREATE_PERSON_TABLE );
      db.execSQL( DatabaseModel.SQL_CREATE_NOTE_TABLE );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion,
                           int newVersion ) {
      // Just drop the old tables for now
      db.execSQL( "DROP TABLE IF EXISTS " + DatabaseModel.PERSON_TABLE_NAME );
      db.execSQL( "DROP TABLE IF EXISTS " + DatabaseModel.NOTE_TABLE_NAME );

      onCreate( db );
    }
  }
  //=========================================================================//


  //=========================================================================//
  // Service Binder
  //=========================================================================//
  public class DatabaseBinder
  extends Binder {
    public DatabaseController getService() {
      return DatabaseController.this;
    }
  }
  //=========================================================================//

}
