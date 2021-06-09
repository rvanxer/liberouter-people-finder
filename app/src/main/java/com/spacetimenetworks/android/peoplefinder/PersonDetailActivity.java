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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.spacetimenetworks.android.peoplefinder.database.DataModel;
import com.spacetimenetworks.android.peoplefinder.database.DatabaseController;


/**
 * Activity that displays details about a person. Takes the person ID as an
 * extra in the Intent, queries database and displays the results.
 *
 * @author teemuk
 */
public class PersonDetailActivity
    extends Activity
    implements DatabaseController.OnInsertedNote {
  // TODO:
  // - Should this take the parsed record or query the database?
  //    -> probably should only take the record since dealing with failure,
  //       e.g., non-existent record is better done by the caller.
  //    -> can arbitrary objects be passed in Intents?

  private static final String TAG =
      PersonDetailActivity.class.getSimpleName();

  public static final String INTENT_EXTRA_PERSON_ID = "personID";

  //=========================================================================//
  // GUI
  //=========================================================================//
  private TextView noteCountText;

  private TextView fullNameText;
  private TextView altNamesText;
  private TextView sexText;
  private TextView ageText;

  private TextView homeStreetText;
  private TextView homeNeighborhoodText;
  private TextView homeCity;
  private TextView homeStateText;
  private TextView homeZipText;
  private TextView homeCountryText;

  private EditText descriptionText;

  private Button addNoteButton;
  private Button seeNotesButton;
  //=========================================================================//


  //=========================================================================//
  // Instance vars
  //=========================================================================//
  private DatabaseController db;
  private ServiceConnection databaseConnection;
  private String personID;
  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    super.setContentView( R.layout.person_details );

    // Setup GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupGuiElements();

    // Get the person record ID that this note will be attached to
    Intent i = super.getIntent();
    this.personID = i.getStringExtra( INTENT_EXTRA_PERSON_ID );
    if ( this.personID == null || this.personID.length() == 0 ) {
      // Can't add a note without attaching to a person
      this.showDialog( "No Person ID provided, cannot display." );
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

    // Remove callback
    if ( this.db != null ) {
      this.db.removeOnInsertedNoteCallback( this );
    }

    this.doUnbindDatabaseService();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d( TAG, "onDestroy()" );
  }
  //=========================================================================//


  //=========================================================================//
  // GUI Callbacks
  //=========================================================================//
  protected void addNoteButtonPushed() {
    Log.d( TAG, "addNoteButtonPushed()" );

    // Start the post note activity
    Intent intent = new Intent();
    intent.setClass( this, PostNoteActivity.class );
    intent.putExtra( PostNoteActivity.INTENT_EXTRA_PERSON_ID,
        this.personID );
    super.startActivity( intent );
  }

  protected void seeNotesButtonPushed() {
    Log.d( TAG, "seeNotesButtonPushed()" );

    // Start the note view activity
    Intent intent = new Intent();
    intent.setClass( this, NoteViewActivity.class );
    intent.putExtra( NoteViewActivity.INTENT_EXTRA_PERSON_ID,
        this.personID );
    super.startActivity( intent );
  }
  //=========================================================================//


  //=========================================================================//
  // DatabaseController.OnInsertedNote
  //-------------------------------------------------------------------------//
  // Listen for notes inserted into the database and refresh the count shown
  // in the GUI if note on this person arrives.
  //=========================================================================//
  @Override
  public void onInserted( long id, DataModel.LocalNote note ) {
    if ( this.personID.equals( note.note.metadata.personID ) ) {
      this.refreshNoteCount();
    }
  }
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {
    this.noteCountText = super.findViewById( R.id.personDetailNoteCountText );
    this.fullNameText = super.findViewById( R.id.personDetailFullNameText );
    this.altNamesText = super.findViewById( R.id.personDetailAltNamesText );
    this.sexText = super.findViewById( R.id.personDetailSexText );
    this.ageText = super.findViewById( R.id.personDetailAgeText );

    this.homeStreetText = super.findViewById( R.id.personDetailStreetText );
    this.homeNeighborhoodText = super.findViewById( R.id.personDetailNeighborhoodText );
    this.homeCity = super.findViewById( R.id.personDetailCityText );
    this.homeStateText = super.findViewById( R.id.personDetailStateText );
    this.homeZipText = super.findViewById( R.id.personDetailZipText );
    this.homeCountryText = super.findViewById( R.id.personDetailCountryText );

    this.descriptionText = super.findViewById( R.id.personDetailDescriptionText );

    this.addNoteButton = super.findViewById( R.id.personDetailNewNoteButton );
    this.seeNotesButton = super.findViewById( R.id.personDetailSeeNotesButton );
  }

  private void setupGuiCallbacks() {
    this.addNoteButton.setOnClickListener(
        view -> PersonDetailActivity.this.addNoteButtonPushed()
    );

    this.seeNotesButton.setOnClickListener(
        view -> PersonDetailActivity.this.seeNotesButtonPushed()
    );
  }

  private void setupGuiElements() {

  }

  private void showDialog( String message ) {
    AlertDialog.Builder builder = new AlertDialog.Builder( this );
    builder.setMessage( message );
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  /**
   * Loads the record from the database and refreshes the GUI.
   */
  private void refreshView() {
    // Precondition check
    if ( this.db == null ) {
      Log.d( TAG, "Couldn't refresh view, no database connection." );
      return;
    }
    if ( this.personID == null || this.personID.length() == 0 ) {
      Log.d( TAG, "Couldn't refresh view, no person ID found." );
      return;
    }

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
          runOnUiThread( () -> {
            fullNameText
                .setText( row.person.identity.name.fullName );
            setOrHide( altNamesText,
                row.person.identity.name.alternateNames );

            setOrHide( sexText, row.person.identity.sex );
            setOrHide( ageText, row.person.identity.age );

            setOrHide( homeStreetText,
                row.person.identity.home.street );
            setOrHide( homeNeighborhoodText,
                row.person.identity.home.neighborhood );
            setOrHide( homeCity, row.person.identity.home.city );
            setOrHide( homeStateText,
                row.person.identity.home.state );
            setOrHide( homeZipText, row.person.identity.home.zip );
            setOrHide( homeCountryText,
                row.person.identity.home.country );

            descriptionText
                .setText( row.person.identity.description );
          } );
        }
    );

    this.refreshNoteCount();
  }

  private void refreshNoteCount() {
    // Query database for note count
    this.db.getNotesForPerson(
        // Person to query
        this.personID,
        // Callback
        rows -> {
          // Update the fields from the GUI thread
          runOnUiThread( () -> noteCountText.setText( "" + rows.size() ) );
        }
    );
  }

  private void setOrHide( TextView view, String text ) {
    if ( text != null ) {
      view.setText( text );
    } else {
      view.setVisibility( View.GONE );
    }
  }

  private void setOrHide( TextView view, Long value ) {
    if ( value != null && value > 0 ) {
      view.setText( "" + value );
    } else {
      view.setVisibility( View.GONE );
    }
  }
  //=========================================================================//


  //=========================================================================//
  // Binding to DatabaseController
  //------------------------------------------------------------------------//
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

        // Get the database reference
        DatabaseController.DatabaseBinder binder =
            ( DatabaseController.DatabaseBinder ) iBinder;
        PersonDetailActivity.this.db = binder.getService();

        // Refresh the view
        PersonDetailActivity.this.refreshView();

        // Set us as a listener for updates
        PersonDetailActivity.this.db
            .addOnInsertedNoteCallback( PersonDetailActivity.this );
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        PersonDetailActivity.this.db = null;
      }
    };
  }
  //=========================================================================//
}
