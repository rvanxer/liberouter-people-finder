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
import android.widget.Button;

import com.spacetimenetworks.android.peoplefinder.database.DatabaseController;

public class MainActivity
    extends Activity {

  private static final String TAG = MainActivity.class.getSimpleName();

  //=========================================================================//
  // GUI elements
  //=========================================================================//
  private Button searchButton;
  private Button postButton;
  //=========================================================================//


  //=========================================================================//
  // Instance vars
  //=========================================================================//
  private DatabaseController db;
  private ServiceConnection databaseConnection;
  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//

  /** Called when the activity is first created. */
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.main );

    // GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();

    // Services
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
  private void searchButtonPushed() {
    Intent intent = new Intent();
    intent.setClass( this, PeopleViewActivity.class );
    super.startActivity( intent );
  }

  private void postButtonPushed() {
    Intent intent = new Intent();
    intent.setClass( this, PostPersonActivity.class );
    super.startActivity( intent );
  }
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {
    this.searchButton = super.findViewById( R.id.SearchButton );
    this.postButton = super.findViewById( R.id.PostButton );
  }

  private void setupGuiCallbacks() {
    this.searchButton.setOnClickListener(
        view -> MainActivity.this.searchButtonPushed()
    );

    this.postButton.setOnClickListener(
        view -> MainActivity.this.postButtonPushed()
    );
  }
  //=========================================================================//


  //=========================================================================//
  // Private - Service handling
  //=========================================================================//
  private void startServices() {
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

        DatabaseController.DatabaseBinder binder =
            ( DatabaseController.DatabaseBinder ) iBinder;
        MainActivity.this.db = binder.getService();
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        MainActivity.this.db = null;
      }
    };
  }
  //=========================================================================//
}
