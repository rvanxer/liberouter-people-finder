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
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom Application subclass to manage the {@link AppLibService} lifecycle. This seems like the
 * cleanest way to make sure the service is started every time the app is used, especially after
 * having been killed by the Oreo background execution limits. The alternative would be to replicate
 * the service management in every activity, or to move to a single activity architecture.
 */
public final class PeopleFinderApplication
extends Application {


  //=========================================================================//
  // Constants
  //=========================================================================//
  private static final String TAG = PeopleFinderApplication.class.getSimpleName();
  private static final String ROOT_DIR = "PeopleFinder";
  private static final String PIC_DIR = "pics";
  //=========================================================================//


  //=========================================================================//
  // Lifecycle
  //=========================================================================//
  @Override
  public void onCreate() {
    Log.d( TAG, "onCreate()" );
    super.onCreate();

    super.registerActivityLifecycleCallbacks( new ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated( @NonNull Activity activity, @Nullable Bundle savedInstanceState ) {
        Log.d( TAG, "onActivityCreated() " + activity.getLocalClassName() );
      }

      @Override
      public void onActivityStarted( @NonNull Activity activity ) {
        Log.d( TAG, "onActivityStarted() " + activity.getLocalClassName() );
        Log.d( TAG, "AppLibService.isRunning: " + AppLibService.isRunning );
        if ( !AppLibService.isRunning ) PeopleFinderApplication.this.startAppLibService();
      }

      @Override
      public void onActivityResumed( @NonNull Activity activity ) {
        Log.d( TAG, "onActivityResumed() " + activity.getLocalClassName() );
        Log.d( TAG, "AppLibService.isRunning: " + AppLibService.isRunning );
      }

      @Override
      public void onActivityPaused( @NonNull Activity activity ) {
        Log.d( TAG, "onActivityPaused() " + activity.getLocalClassName() );
      }

      @Override
      public void onActivityStopped( @NonNull Activity activity ) {
        Log.d( TAG, "onActivityStopped() " + activity.getLocalClassName() );
      }

      @Override
      public void onActivitySaveInstanceState( @NonNull Activity activity, @NonNull Bundle outState ) {
        Log.d( TAG, "onActivitySaveInstanceState() " + activity.getLocalClassName() );
      }

      @Override
      public void onActivityDestroyed( @NonNull Activity activity ) {
        Log.d( TAG, "onActivityDestroyed() " + activity.getLocalClassName() );
      }
    } );
  }
  //=========================================================================//


  //=========================================================================//
  // Service handling
  //=========================================================================//
  private void startAppLibService() {
    File picDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS );
    picDir = new File( picDir, ROOT_DIR );
    picDir = new File( picDir, PIC_DIR );
    final Intent i = new Intent( super.getApplicationContext(), AppLibService.class );
    i.putExtra( AppLibService.INTENT_EXTRA_PIC_DIR_PATH,
        picDir.getAbsolutePath() );
    super.startService( i );
  }
  //=========================================================================//
}
