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
public class PersonsListAdapter
    extends BetterListAdapter<DataModel.PersonName> {
  static final String TAG = PersonsListAdapter.class.getSimpleName();


  //==========================================================================//
  // Instance vars
  //==========================================================================//
  // Formatting of the timestamp in list items
  private String datePattern = "HH:mm:ss d.M.yyyy";
  private SimpleDateFormat timeformatter = new SimpleDateFormat( datePattern );
  //==========================================================================//


  //==========================================================================//
  // API
  //==========================================================================//
  public PersonsListAdapter( Context context, int rowResourceId,
                             List<DataModel.PersonName> items ) {
    super( context, rowResourceId, items );
  }
  //==========================================================================//


  //==========================================================================//
  // BetterListAdapter implementation
  //==========================================================================//
  @Override
  protected void populateView( View rowLayout,
                               DataModel.PersonName item ) {
    // Get the elements
    TextView nameText =
        ( TextView ) rowLayout.findViewById( R.id.nameRowName );
    TextView statusText =
        ( TextView ) rowLayout.findViewById( R.id.nameRowStatus );
    TextView dateText =
        ( TextView ) rowLayout.findViewById( R.id.nameRowDate );

    // Populate the elements
    nameText.setText( item.fullName );
    statusText.setText( item.status );
    dateText.setText(
        this.timeformatter.format( new Date( item.entryDate * 1000 ) )
    );
  }
  //==========================================================================//
}
