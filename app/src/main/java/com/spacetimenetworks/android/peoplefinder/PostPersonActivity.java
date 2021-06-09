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
import android.widget.EditText;
import android.widget.Spinner;

import com.spacetimenetworks.android.peoplefinder.database.DataModel;
import com.spacetimenetworks.android.peoplefinder.database.DatabaseController;

import java.util.Random;

/**
 * Activity for creating a new PERSON record.
 *
 * @author teemuk
 */
public class PostPersonActivity
    extends Activity {
  private static final String TAG = PostPersonActivity.class.getSimpleName();

  /** Domain to use for records. TODO: make this unique per app instance. */
  public static final String RECORD_DOMAIN = "scampi-people-finder";
  public static final long RECORD_TTL = 30 * 24 * 60 * 60; // 30 days

  //=========================================================================//
  // GUI Elements
  //=========================================================================//
  private EditText familyNameField;
  private EditText givenNameField;
  private EditText altNamesField;

  private Spinner sexSpinner;
  private EditText ageField;

  private EditText streetField;
  private EditText neighborhoodField;
  private EditText cityField;
  private EditText stateField;
  private EditText zipField;
  private EditText countryField;

  private EditText descriptionField;

  private Button addPhotoButton;
  private Button publishButton;
  //=========================================================================//


  //=========================================================================//
  // Instance vars
  //=========================================================================//
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

    super.setContentView( R.layout.person_input );

    // Setup GUI
    this.setupGuiReferences();
    this.setupGuiCallbacks();
    this.setupGuiElements();
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
  // GUI Callbacks
  //=========================================================================//
  private void publishButtonPushed() {
    Log.d( TAG, "publishButtonPushed" );

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
    this.publishButton.setEnabled( false );

    // Build a person record from the GUI state
    DataModel.LocalPerson record = this.createPersonRecord();

    // Give to the database
    this.db.insertPerson(
        // Record to insert
        record,
        // Callback invoked after the data has been inserted
        ( id, row ) -> {
          Log.d( TAG, "Inserted person: " +
                      row.person.identity.name.fullName );
          // Return from the activity
          // TODO: can this be called from non-GUI thread?
          PostPersonActivity.super.finish();
        } );
  }

  private void addPhotoButtonPushed() {
    Log.d( TAG, "photoButtonPushed" );
  }
  //=========================================================================//


  //=========================================================================//
  // Private
  //=========================================================================//

  /**
   * Creates a person record from the current state of GUI elements.
   *
   * @return {@code PersonRecord} populated with the data from the GUI.
   */
  private DataModel.LocalPerson createPersonRecord() {
    // Collect the data
    String familyName = this.familyNameField.getText().toString();
    String givenName = this.givenNameField.getText().toString();
    String altNames = this.altNamesField.getText().toString();
    if ( altNames.length() == 0 ) altNames = null;
    String sex = this.sexSpinner.getSelectedItem().toString();
    String ageStr = this.ageField.getText().toString();
    Long age = null;
    if ( ageStr.length() > 0 ) {
      age = Long.parseLong( ageStr );
    }
    String homeStreet = this.streetField.getText().toString();
    if ( homeStreet.length() == 0 ) homeStreet = null;
    String homeNeighborhood = this.neighborhoodField.getText().toString();
    if ( homeNeighborhood.length() == 0 ) homeNeighborhood = null;
    String homeCity = this.cityField.getText().toString();
    if ( homeCity.length() == 0 ) homeCity = null;
    String homeState = this.stateField.getText().toString();
    if ( homeState.length() == 0 ) homeState = null;
    String homeZip = this.zipField.getText().toString();
    if ( homeZip.length() == 0 ) homeZip = null;
    String homeCountry = this.countryField.getText().toString();
    if ( homeCountry.length() == 0 ) homeCountry = null;
    String description = this.descriptionField.getText().toString();
    if ( description.length() == 0 ) description = null;

    // Generate data
    String fullName = givenName + " " + familyName;
    String id = "" + this.rng.nextLong();
    id = id.replace( "-", "" );
    // TODO: should check if this is unique
    String recordID = RECORD_DOMAIN + "/" + id;
    long entryDate = System.currentTimeMillis() / 1000;
    long expiryDate = entryDate + RECORD_TTL;

    // Validate
    if ( givenName.length() == 0 ) {
      throw new IllegalStateException( "Missing given name." );
    }
    if ( familyName.length() == 0 ) {
      throw new IllegalStateException( "Missing family name." );
    }

    // Create the record
    DataModel.Person person =
        new DataModel.Person(
            recordID,         // record ID
            entryDate,        // entry date
            expiryDate,       // expiry date
            null,             // author name
            null,             // author email
            null,             // author phone
            null,             // source name
            null,             // source date
            null,             // source URL
            fullName,         // full name
            givenName,        // given name
            familyName,       // family name
            altNames,         // alternate names
            description,      // description
            sex,              // sex
            null,             // date of birth
            age,              // age
            homeStreet,       // home street
            homeNeighborhood, // home neighborhood
            homeCity,         // home city
            homeState,        // home home state
            homeZip,          // home zip
            homeCountry,      // home country
            null,             // photo url
            null              // profile urls
        );

    return new DataModel.LocalPerson( person, null, false );
  }
  //=========================================================================//


  //=========================================================================//
  // Private - GUI
  //=========================================================================//
  private void setupGuiReferences() {
    this.familyNameField = super.findViewById( R.id.postPersonFamilyNameText );
    this.givenNameField = super.findViewById( R.id.postPersonGivenNameText );
    this.altNamesField = super.findViewById( R.id.postPersonAltNamesText );

    this.sexSpinner = super.findViewById( R.id.postPersonSex );
    this.ageField = super.findViewById( R.id.postPersonAgeText );

    this.streetField = super.findViewById( R.id.postPersonStreetText );
    this.neighborhoodField = super.findViewById( R.id.postPersonNghbrhoodText );
    this.cityField = super.findViewById( R.id.postPersonCityText );
    this.stateField = super.findViewById( R.id.postPersonStateText );
    this.zipField = super.findViewById( R.id.postPersonZipText );
    this.countryField = super.findViewById( R.id.postPersonCountryText );

    this.descriptionField = super.findViewById( R.id.postPersonDescText );

    this.addPhotoButton = super.findViewById( R.id.postPersonPhotoButton );
    this.publishButton = super.findViewById( R.id.postPersonPublishButton );
  }

  private void setupGuiCallbacks() {
    this.addPhotoButton.setOnClickListener(
        view -> PostPersonActivity.this.addPhotoButtonPushed()
    );

    this.publishButton.setOnClickListener(
        view -> PostPersonActivity.this.publishButtonPushed()
    );
  }

  private void setupGuiElements() {
    this.setupSexSpinner( this.sexSpinner );
  }

  private void setupSexSpinner( Spinner spinner ) {
    ArrayAdapter<CharSequence> adapter
        = ArrayAdapter.createFromResource( this,
        R.array.sex_choices,
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
    if ( this.familyNameField.length() == 0 ) {
      this.familyNameField.requestFocus();
      this.showDialog( "Family name is required" );

      return false;
    }
    if ( this.givenNameField.length() == 0 ) {
      this.givenNameField.requestFocus();
      this.showDialog( "Given name is required" );

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
        PostPersonActivity.this.db = binder.getService();
      }

      @Override
      public void onServiceDisconnected( ComponentName componentName ) {
        PostPersonActivity.this.db = null;
      }
    };
  }
  //=========================================================================//
}
