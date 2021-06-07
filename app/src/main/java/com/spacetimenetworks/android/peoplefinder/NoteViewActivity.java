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
import android.widget.ListView;
import android.widget.TextView;

import com.spacetimenetworks.android.peoplefinder.database.DataModel;
import com.spacetimenetworks.android.peoplefinder.database.DatabaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays notes for a given person. The person ID is passed in
 * the Intent that launches this activity.
 *
 * @author teemuk
 */
public class NoteViewActivity
    extends Activity
    implements DatabaseController.OnInsertedNote {
  private static final String TAG = PeopleViewActivity.class.getSimpleName();

  public static final String INTENT_EXTRA_PERSON_ID = "personID";

  //=========================================================================//
  // Instance vars
  //=========================================================================//
  private DatabaseController db;
  private ServiceConnection databaseConnection;
  private NoteListAdapter adapter;
  private String personID;
  //=========================================================================//


  //=========================================================================//
  // GUI elements
  //=========================================================================//
  private ListView noteList;
  private TextView nameText;
  private Button addButton;
  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    super.setContentView( R.layout.note_list_view );

    // Setup GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupGuiElements();

    // Get the person record ID that this note will be attached to
    Intent i = super.getIntent();
    this.personID = i.getStringExtra( INTENT_EXTRA_PERSON_ID );
    if ( this.personID == null || this.personID.length() == 0 ) {
      // Can't add a note without attaching to a person
      this.showDialog( "No Person ID provided, cannot display notes." );
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

    // Remove callbacks
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
  // GUI callbacks
  //=========================================================================//
  protected void addButtonPushed() {
    Log.d( TAG, "addButtonPushed()" );

    // Start activity
    Intent intent = new Intent();
    intent.setClass( this, PostNoteActivity.class );
    intent.putExtra( PostNoteActivity.INTENT_EXTRA_PERSON_ID,
        this.personID );
    super.startActivity( intent );
  }
  //=========================================================================//


  //=========================================================================//
  // DatabaseController callbacks
  //-------------------------------------------------------------------------//
  // Listen for notes inserted into the database and update the GUI.
  //=========================================================================//
  @Override
  public void onInserted( long id, DataModel.LocalNote note ) {
    if ( this.personID.equals( note.note.metadata.personID ) ) {
      this.refreshListViewFromDatabase();
    }
  }
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {
    this.nameText =
        ( TextView ) ( super.findViewById( R.id.noteViewNameText ) );
    this.noteList =
        ( ListView ) ( super.findViewById( R.id.noteViewList ) );
    this.addButton =
        ( Button ) ( super.findViewById( R.id.noteViewAddButton ) );
  }

  private void setupGuiCallbacks() {
    // Add new record button
    this.addButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick( View view ) {
            NoteViewActivity.this.addButtonPushed();
          }
        }
    );
  }

  private void setupGuiElements() {
    this.setupListView();
  }

  private void setupListView() {
    // Create adapter for the list
    this.adapter =
        new NoteListAdapter(
            this, R.layout.note_list_row,
            new ArrayList<DataModel.LocalNote>( 0 ) );

    // Set the adapter
    this.noteList.setAdapter( this.adapter );
    this.adapter.notifyDataSetChanged();
  }

  /**
   * Runs a database query and updates the list view adapter with the results.
   */
  private void refreshListViewFromDatabase() {
    if ( this.db == null ) {
      Log.d( TAG, "Couldn't update list view. No database connection." );
      return;
    }
    if ( this.personID == null || this.personID.length() == 0 ) {
      Log.d( TAG, "Couldn't update list view. No person ID found." );
      return;
    }

    // Run a query
    this.db.getNotesForPerson(
        // Person ID to get notes for
        this.personID,
        // Callback after results arrive
        new DatabaseController.GetNotesQueryFinished() {
          @Override
          public void queryFinished(
              final List<DataModel.LocalNote> rows ) {
            // Must run this on the GUI thread
            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.d( TAG, "Setting items in adapter." );
                    adapter.setItems( rows );
                  }
                }
            );
          }
        }
    );

    // Fetch the name from the database
    // Query database for person details
    this.db.getPersonDetails(
        // Person ID to query
        this.personID,
        // Result handling
        new DatabaseController.GetPersonDetailsQueryFinished() {
          @Override
          public void queryFinished(
              final DataModel.LocalPerson row ) {
            // Precondition check
            if ( row == null ) {
              Log.d( TAG, "Couldn't refresh view, no person ID '" +
                          personID + "' found in database." );
              return;
            }

            // Update the fields from the GUI thread
            runOnUiThread( new Runnable() {
              @Override
              public void run() {
                nameText.setText( row.person.identity.name.fullName );
              }
            } );
          }
        }
    );
  }

  private void showDialog( String message ) {
    AlertDialog.Builder builder = new AlertDialog.Builder( this );
    builder.setMessage( message );
    AlertDialog dialog = builder.create();
    dialog.show();
  }
  //=========================================================================//


  //=========================================================================//
  // Binding to DatabaseController
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

        // Set references
        DatabaseController.DatabaseBinder binder =
            ( DatabaseController.DatabaseBinder ) iBinder;
        NoteViewActivity.this.db = binder.getService();

        // Populate the list view
        NoteViewActivity.this.refreshListViewFromDatabase();

        // Set us as a listener for updates
        NoteViewActivity.this.db
            .addOnInsertedNoteCallback( NoteViewActivity.this );
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        NoteViewActivity.this.db = null;
      }
    };
  }
  //=========================================================================//
}
