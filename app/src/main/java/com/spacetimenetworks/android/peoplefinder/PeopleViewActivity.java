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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.spacetimenetworks.android.peoplefinder.database.DataModel;
import com.spacetimenetworks.android.peoplefinder.database.DatabaseController;

import java.util.ArrayList;


/**
 * Activity for displaying all known people from the database.
 *
 * @author teemuk
 */
public class PeopleViewActivity
extends Activity
implements DatabaseController.OnInsertedNote,
    DatabaseController.OnInsertedPerson {
  private static final String TAG = PeopleViewActivity.class.getSimpleName();

  private static final String ROOT_DIR = "PeopleFinder";
  private static final String PIC_DIR = "pics";

  //=========================================================================//
  // Instance vars
  //=========================================================================//
  private DatabaseController db;
  private ServiceConnection databaseConnection;
  private PersonsListAdapter adapter;
  //=========================================================================//


  //=========================================================================//
  // GUI elements
  //=========================================================================//
  private ListView nameList;
  private Button addButton;
  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    super.setContentView( R.layout.people_view );

    // Setup GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupGuiElements();

    // Start services (this is the main activity)
    this.startServices();
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
      this.db.removeOnInsertedPersonCallback( this );
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
    intent.setClass( this, PostPersonActivity.class );
    super.startActivity( intent );
  }

  protected void listElementPushed( AdapterView<?> adapterView,
                                    View view, int position, long id ) {
    Log.d( TAG, "listElementPushed() position = " + position );

    // Grab the clicked item
    DataModel.PersonName name =
        ( DataModel.PersonName ) ( this.adapter
            .getItem( position ) );
    if ( name == null ) {
      Log.d( TAG, "Couldn't get the requested row. Aborting" );
      return;
    }

    // Start the detail view activity
    // Start activity
    Intent intent = new Intent();
    intent.setClass( this, PersonDetailActivity.class );
    intent.putExtra( PersonDetailActivity.INTENT_EXTRA_PERSON_ID,
        name.recordID );
    super.startActivity( intent );
  }
  //=========================================================================//


  //=========================================================================//
  // DatabaseController callbacks
  //-------------------------------------------------------------------------//
  // Listen for notes and people inserted into the database and update the
  // GUI.
  //=========================================================================//
  @Override
  public void onInserted( long id, DataModel.LocalNote note ) {
    this.refreshListViewFromDatabase();
  }

  @Override
  public void onInserted( long id, DataModel.LocalPerson person ) {
    this.refreshListViewFromDatabase();
  }
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {
    this.nameList = super.findViewById( R.id.peopleViewList );
    this.addButton = super.findViewById( R.id.peopleViewAddButton );
  }

  private void setupGuiCallbacks() {
    // Add new record button
    this.addButton.setOnClickListener(
        view -> PeopleViewActivity.this.addButtonPushed()
    );

    // People view list
    this.nameList.setOnItemClickListener(
        PeopleViewActivity.this::listElementPushed
    );
  }

  private void setupGuiElements() {
    this.setupListView();
  }

  private void setupListView() {
    // Create adapter for the list
    this.adapter =
        new PersonsListAdapter(
            this, R.layout.name_list_row,
            new ArrayList<>( 0 ) );

    // Set the adapter
    this.nameList.setAdapter( this.adapter );
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

    // Run a query
    this.db.findAllPersons(
        names -> {
          // Must run this on the GUI thread
          runOnUiThread(
              () -> {
                Log.d( TAG, "Setting items in adapter." );
                adapter.setItems( names );
              }
          );
        }
    );
  }
  //=========================================================================//

  //=========================================================================//
  // Private - Service handling
  //=========================================================================//
  private void startServices() {
    // Start database
    super.startService( new Intent( this, DatabaseController.class ) );
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

        // Set references
        DatabaseController.DatabaseBinder binder =
            ( DatabaseController.DatabaseBinder ) iBinder;
        PeopleViewActivity.this.db = binder.getService();

        // Populate the list view
        PeopleViewActivity.this.refreshListViewFromDatabase();

        // Set listeners
        PeopleViewActivity.this.db
            .addOnInsertedNoteCallback( PeopleViewActivity.this );
        PeopleViewActivity.this.db
            .addOnInsertedPersonCallback( PeopleViewActivity.this );
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        Log.d( TAG, "Database disconnected" );
        PeopleViewActivity.this.db = null;
      }
    };
  }
  //=========================================================================//
}
