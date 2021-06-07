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

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.spacetimenetworks.android.peoplefinder.database.DataModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Adapter for items in the persons list view.
 *
 * @author teemuk
 */
public class NoteListAdapter
    extends BetterListAdapter<DataModel.LocalNote> {
  static final String TAG = PersonsListAdapter.class.getSimpleName();

  //==========================================================================//
  // Instance vars
  //==========================================================================//
  // Formatting of the timestamp in list items
  private final String datePattern = "HH:mm:ss d.M.yyyy";
  private final SimpleDateFormat timeformatter = new SimpleDateFormat( datePattern );
  //==========================================================================//

  //==========================================================================//
  // API
  //==========================================================================//
  public NoteListAdapter( Context context, int rowResourceId,
                          List<DataModel.LocalNote> items ) {
    super( context, rowResourceId, items );
  }
  //==========================================================================//


  //==========================================================================//
  // BetterListAdapter implementation
  //==========================================================================//
  @Override
  protected void populateView( View rowLayout,
                               DataModel.LocalNote item ) {
    // Get the elements
    TextView authorNameTitle =
        ( TextView ) rowLayout.findViewById( R.id.noteRowAuthorTitle );
    TextView authorNameText =
        ( TextView ) rowLayout.findViewById( R.id.noteRowAuthorText );

    TextView dateText =
        ( TextView ) rowLayout.findViewById( R.id.noteRowDateText );

    EditText contentText =
        ( EditText ) rowLayout.findViewById( R.id.noteRowContentText );

    TextView statusTitle =
        ( TextView ) rowLayout.findViewById( R.id.noteRowStatusTitle );
    TextView statusText =
        ( TextView ) rowLayout.findViewById( R.id.noteRowStatusText );

    TextView locationTitle =
        ( TextView ) rowLayout.findViewById( R.id.noteRowLocationTitle );
    TextView locationText =
        ( TextView ) rowLayout.findViewById( R.id.noteRowLocationText );


    // Populate the elements
    this.setOrHide( authorNameTitle, authorNameText,
        item.note.metadata.authorName );
    String date = this.timeformatter.format(
        new Date( item.note.metadata.entryDate * 1000 ) );
    this.setOrHide( dateText, date );
    if ( item.note.status.text != null ) {
      contentText.setText( item.note.status.text );
    }
    this.setOrHide( statusTitle, statusText, item.note.status.status );
    this.setOrHide( locationTitle, locationText,
        item.note.status.lastKnownLocation );
  }
  //==========================================================================//


  //==========================================================================//
  // Private
  //==========================================================================//
  private void setOrHide( TextView titleView, TextView contentView,
                          String content ) {
    if ( content != null && content.length() > 0 ) {
      contentView.setText( content );
    } else {
      titleView.setVisibility( View.GONE );
      contentView.setVisibility( View.GONE );
    }
  }

  private void setOrHide( TextView contentView,
                          String content ) {
    if ( content != null && content.length() > 0 ) {
      contentView.setText( content );
    } else {
      contentView.setVisibility( View.GONE );
    }
  }
  //==========================================================================//
}
