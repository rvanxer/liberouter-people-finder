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
package com.spacetimenetworks.android.peoplefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.spacetimenetworks.android.peoplefinder.database.DataModel;
import com.spacetimenetworks.android.peoplefinder.database.DatabaseController;

import java.util.Random;

/**
 * Activity to insert a NOTE about a PERSON into the database.
 *
 * @author teemuk
 */
public class PostNoteActivity
    extends Activity {
  public static final String TAG = PostNoteActivity.class.getSimpleName();

  public static final String INTENT_EXTRA_PERSON_ID = "personID";
  /** Domain to use for records. TODO: make this unique per app instance. */
  public static final String RECORD_DOMAIN = "scampi-people-finder";

  //=========================================================================//
  // GUI Elements
  //=========================================================================//
  private TextView nameText;
  private Spinner statusSpinner;
  private RadioButton contactYesRadio;
  private TextView messageText;
  private TextView locationText;
  private TextView authorText;
  private Button sendButton;
  //=========================================================================//


  //=========================================================================//
  // Instance vars
  //=========================================================================//
  private String personID;
  private DatabaseController db;
  private ServiceConnection databaseConnection;
  private final Random rng = new Random();
  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    super.setContentView( R.layout.status_input );

    // Setup GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupGuiElements();

    // Get the person record ID that this note will be attached to
    Intent i = super.getIntent();
    this.personID = i.getStringExtra( INTENT_EXTRA_PERSON_ID );
    if ( this.personID == null ) {
      // Can't add a note without attaching to a person
      this.showDialog( "No Person ID provided, cannot add a note." );
      super.finish();
    }
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d( TAG, "onStart()" );

    // Connect database
    this.doBindDatabaseService();
  }

  @Override
  public void onRestart() {
    super.onRestart();

    Log.d( TAG, "onRestart()" );
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d( TAG, "onResume()" );
  }

  @Override
  public void onPause() {
    super.onPause();

    Log.d( TAG, "onPause()" );
  }

  @Override
  public void onStop() {
    super.onStop();

    Log.d( TAG, "onStop()" );

    this.doUnbindDatabaseService();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d( TAG, "onDestroy()" );
  }
  //=========================================================================//


  //=========================================================================//
  // GUI callbacks
  //=========================================================================//
  private void postButtonPushed() {
    // Precondition check
    if ( this.db == null ) {
      Log.e( TAG, "No database connection, cannot publish record." );
      return;
    }

    // Make sure all required fields are filled
    if ( !this.checkRequiredInputState() ) {
      return;
    }

    // Disallow further taps (database operation is async and we don't want
    // multiple button pushes)
    this.sendButton.setEnabled( false );

    // Build a person record from the GUI state
    DataModel.LocalNote note = this.createNote();

    // Give to the database
    this.db.insertNote(
        // Note to insert
        note,
        // Callback invoked after the data has been inserted
        ( id, row ) -> {
          Log.d( TAG, "Inserted Note: " +
                      row.note.metadata.recordID );
          // Return from the activity
          // TODO: can this be called from non-GUI thread?
          PostNoteActivity.super.finish();
        }
    );
  }
  //=========================================================================//


  //=========================================================================//
  // Private
  //=========================================================================//

  /**
   * Creates a note from the current state of the GUI.
   *
   * @return Note that is populated from the current state of the GUI.
   */
  private DataModel.LocalNote createNote() {
    // Collect the data
    String id = "" + this.rng.nextLong();
    id = id.replace( "-", "" );
    // TODO: should check if this is unique
    String recordID = RECORD_DOMAIN + "/" + id;
    String personID = this.personID;
    Long entryDate = System.currentTimeMillis() / 1000L;
    String authorName = this.authorText.getText().toString();
    Boolean madeContact = this.contactYesRadio.isChecked();
    String status = this.statusSpinner.getSelectedItem().toString();
    String location = this.locationText.getText().toString();
    if ( location.length() == 0 ) location = null;
    String text = this.messageText.getText().toString();

    // Create new note
    DataModel.Note note = new DataModel.Note(
        recordID,      // recordID
        personID,      // personID
        null,          // linkedPersonID
        entryDate,     // entryDate
        authorName,    // authorName
        null,          // authorEmail
        null,          // authorPhone
        null,          // sourceDate
        madeContact,   // authorMadeContact
        status,        // status
        null,          // emailOfFoundPerson
        null,          // phoneOfFoundPerson
        location,      // lastKnownLocation
        text,          // text
        null           // photoUrl
    );
    return new DataModel.LocalNote( note, null, false );
  }
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {
    this.nameText =
        ( TextView ) super.findViewById( R.id.addNoteNameText );
    this.statusSpinner =
        ( Spinner ) super.findViewById( R.id.addNoteStatusSpinner );
    this.contactYesRadio =
        ( RadioButton ) super.findViewById( R.id.addNoteContactYesRadio );
    this.messageText =
        ( TextView ) super.findViewById( R.id.addNoteMessageText );
    this.locationText =
        ( TextView ) super.findViewById( R.id.addNoteLocationText );
    this.authorText =
        ( TextView ) super.findViewById( R.id.addNoteAuthorText );
    this.sendButton =
        ( Button ) super.findViewById( R.id.addNoteButton );
  }

  private void setupGuiCallbacks() {
    this.sendButton.setOnClickListener(
        view -> PostNoteActivity.this.postButtonPushed()
    );
  }

  private void setupGuiElements() {
    this.setupStatusSpinner( this.statusSpinner );
  }

  private void setupStatusSpinner( Spinner spinner ) {
    ArrayAdapter<CharSequence> adapter
        = ArrayAdapter.createFromResource( this,
        R.array.status_choices,
        android.R.layout
            .simple_spinner_item );
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item );
    // Apply the adapter to the spinner
    spinner.setAdapter( adapter );
  }

  /**
   * Checks if all the required fields are filled in the GUI.
   *
   * @return {@code true} if required fields are filled, {@code false}
   * otherwise.
   */
  private boolean checkRequiredInputState() {
    if ( this.messageText.length() == 0 ) {
      this.messageText.requestFocus();
      this.showDialog( "Message is required" );

      return false;
    }
    if ( this.authorText.length() == 0 ) {
      this.authorText.requestFocus();
      this.showDialog( "Author name is required" );

      return false;
    }
    return true;
  }

  private void showDialog( String message ) {
    AlertDialog.Builder builder = new AlertDialog.Builder( this );
    builder.setMessage( message );
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  private void refreshFromDatabase() {
    // Precondition check
    if ( this.db == null ) {
      Log.d( TAG, "Couldn't refresh view, no database connection." );
      return;
    }
    if ( this.personID == null || this.personID.length() == 0 ) {
      Log.d( TAG, "Couldn't refresh view, no person ID found." );
      return;
    }

    // Fetch the name from the database
    // Query database for person details
    this.db.getPersonDetails(
        // Person ID to query
        this.personID,
        // Result handling
        row -> {
          // Precondition check
          if ( row == null ) {
            Log.d( TAG, "Couldn't refresh view, no person ID '" +
                        personID + "' found in database." );
            return;
          }

          // Update the fields from the GUI thread
          runOnUiThread( () -> nameText.setText( row.person.identity.name.fullName ) );
        }
    );
  }
  //=========================================================================//


  //=========================================================================//
  // Binding to DatabaseController
  //-------------------------------------------------------------------------//
  // The AppLib service binds to the database controller in order to insert
  // received messages and to publish newly generated messages.
  //=========================================================================//
  private void doBindDatabaseService() {
    this.databaseConnection = this.getServiceConnection();
    super.bindService( new Intent( this, DatabaseController.class ),
        this.databaseConnection, Context.BIND_AUTO_CREATE );
  }

  private void doUnbindDatabaseService() {
    super.unbindService( this.databaseConnection );
  }

  private ServiceConnection getServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected( ComponentName componentName,
                                      IBinder iBinder ) {
        if ( !( iBinder instanceof DatabaseController.DatabaseBinder ) ) {
          Log.e( TAG, "Wrong type of binder in onServiceConnected()" );
          return;
        }

        Log.d( TAG, "Database connected" );

        DatabaseController.DatabaseBinder binder =
            ( DatabaseController.DatabaseBinder ) iBinder;
        PostNoteActivity.this.db = binder.getService();

        // Refresh the name field of the GUI
        PostNoteActivity.this.refreshFromDatabase();
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        PostNoteActivity.this.db = null;
      }
    };
  }
  //=========================================================================//

}
