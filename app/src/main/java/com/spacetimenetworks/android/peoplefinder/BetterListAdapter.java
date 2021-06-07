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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * <p> Adapter for items in a list view. The user provides the context, layout
 * resource and a list of items to use for populating the layout. The sub class
 * implements {@link #populateView} to populate the layout with the data from an
 * item. </p> <p/> <p> Items can be changed at any time by calling {@link
 * #setItems}, which needs to be called from the GUI thread in order for the
 * attached list view to update. </p>
 *
 * @author teemuk
 */
public abstract class BetterListAdapter<T>
    extends BaseAdapter {
  // TODO:
  // - Could be made into a generic
  static final String TAG = BetterListAdapter.class.getSimpleName();

  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private final Object itemsLock = new Object();
  private volatile List<T> items;
  private final Context context;
  private final int rowResourceId;
  //==========================================================================//


  //==========================================================================//
  // API
  //==========================================================================//
  public BetterListAdapter( Context context, int rowResourceId,
                            List<T> items ) {
    super();

    this.context = context;
    this.rowResourceId = rowResourceId;
    this.items = items;
  }

  /**
   * Swap in a new list of items. Needs to be called from the GUI thread in
   * order for the attached list view to update.
   *
   * @param items
   *     list of items
   */
  public void setItems( List<T> items ) {
    // Switch the dataset
    synchronized ( this.itemsLock ) {
      this.items = items;
    }

    Log.d( TAG, "New data set, notifying super." );

    // Notify the observers that the data changed
    super.notifyDataSetChanged();
  }

  public T getItemAtPosition( int position ) {
    return this.items.get( position );
  }
  //==========================================================================//


  //==========================================================================//
  // Sub class API
  //------------------------------------------------------------------------//
  // Sub classes implement these methods to populate views from the items
  //==========================================================================//

  /**
   * Asks the sub class to populate the given view with the given item. The
   * view
   * is an instance of the layout whose resource id is provided in the
   * constructor.
   *
   * @param layout
   *     the layout to populate
   * @param item
   *     the item to use for populating the layout
   */
  protected abstract void populateView( final View layout, final T item );
  //==========================================================================//


  //==========================================================================//
  // BaseAdapter implementation
  //==========================================================================//
  @Override
  public int getCount() {
    synchronized ( this.itemsLock ) {
      return items.size();
    }
  }

  @Override
  public Object getItem( int position ) {
    synchronized ( this.itemsLock ) {
      return this.items.get( position );
    }
  }

  @Override
  public long getItemId( int position ) {
    synchronized ( this.itemsLock ) {
      return this.items.get( position ).hashCode();
    }
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getView( int position, View convertView, ViewGroup parent ) {
    // Setup the view that we will bind the data to
    View rowLayout;
    if ( convertView != null ) {
      rowLayout = convertView;
    } else {
      LayoutInflater inflater =
          ( LayoutInflater ) context
              .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      rowLayout = inflater.inflate( this.rowResourceId, parent, false );
    }

    // Get the item
    T item;
    synchronized ( this.itemsLock ) {
      item = this.items.get( position );
    }

    // Populate the elements
    this.populateView( rowLayout, item );

    return rowLayout;
  }
  //==========================================================================//

}
