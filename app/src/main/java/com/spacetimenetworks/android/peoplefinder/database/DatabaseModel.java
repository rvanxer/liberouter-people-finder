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
package com.spacetimenetworks.android.peoplefinder.database;

 final class DatabaseModel {
  private DatabaseModel() {}

  //=========================================================================//
  // Database Definitions
  //=========================================================================//
  /** Database name */
   static final String DB_NAME = "people_finder";
  /**
   * Database version. Increment this is the schema changes, and implement the
   * schema upgrade in DbHelper.onUpgrade().
   */
   static final int DB_VERSION = 2;

  //-------------------------------------------------------------------------//
  // Names for tables and columns
  // - Follows the schema suggested in PFIF 1.4 spec
  //-------------------------------------------------------------------------//
  /** Name for the PERSON table (PFIF 1.4:8.) */
   static final String PERSON_TABLE_NAME
      = "PERSON";

   static final String
  PERSON_COL_RECORD_ID = "person_record_id",
  PERSON_COL_RECORD_ID_TYPE = "TEXT",

  PERSON_COL_ENTRY_DATE = "entry_date",
  PERSON_COL_ENTRY_DATE_TYPE = "INTEGER",

  PERSON_COL_EXPIRY_DATE = "expiry_date",
  PERSON_COL_EXPIRY_DATE_TYPE = "INTEGER",

  PERSON_COL_AUTHOR_NAME = "author_name",
  PERSON_COL_AUTHOR_NAME_TYPE = "TEXT",

  PERSON_COL_AUTHOR_EMAIL = "author_email",
  PERSON_COL_AUTHOR_EMAIL_TYPE = "TEXT",

  PERSON_COL_AUTHOR_PHONE = "author_phone",
  PERSON_COL_AUTHOR_PHONE_TYPE = "TEXT",

  PERSON_COL_SOURCE_NAME = "source_name",
  PERSON_COL_SOURCE_NAME_TYPE = "TEXT",

  PERSON_COL_SOURCE_DATE = "source_date",
  PERSON_COL_SOURCE_DATE_TYPE = "INTEGER",

  PERSON_COL_SOURCE_URL = "source_url",
  PERSON_COL_SOURCE_URL_TYPE = "TEXT",

  PERSON_COL_FULL_NAME = "full_name",
  PERSON_COL_FULL_NAME_TYPE = "TEXT",

  PERSON_COL_GIVEN_NAME = "given_name",
  PERSON_COL_GIVEN_NAME_TYPE = "TEXT",

  PERSON_COL_FAMILY_NAME = "family_name",
  PERSON_COL_FAMILY_NAME_TYPE = "TEXT",

  PERSON_COL_ALT_NAMES = "alternate_names",
  PERSON_COL_ALT_NAMES_TYPE = "TEXT",

  PERSON_COL_DESCRIPTION = "description",
  PERSON_COL_DESCRIPTION_TYPE = "TEXT",

  PERSON_COL_SEX = "sex",
  PERSON_COL_SEX_TYPE = "INTEGER",

  PERSON_COL_DOB = "date_of_birth",
  PERSON_COL_DOB_TYPE = "TEXT",

  PERSON_COL_AGE = "age",
  PERSON_COL_AGE_TYPE = "INTEGER",

  PERSON_COL_HOME_STREET = "home_street",
  PERSON_COL_HOME_STREET_TYPE = "TEXT",

  PERSON_COL_HOME_NGHBRHD = "home_neighborhood",
  PERSON_COL_HOME_NGHBRHD_TYPE = "TEXT",

  PERSON_COL_HOME_CITY = "home_city",
  PERSON_COL_HOME_CITY_TYPE = "TEXT",

  PERSON_COL_HOME_STATE = "home_state",
  PERSON_COL_HOME_STATE_TYPE = "TEXT",

  PERSON_COL_HOME_ZIP = "home_postal_code",
  PERSON_COL_HOME_ZIP_TYPE = "TEXT",

  PERSON_COL_HOME_COUNTRY = "home_country",
  PERSON_COL_HOME_COUNTRY_TYPE = "TEXT",

  PERSON_COL_PHOTO_URL = "photo_url",
  PERSON_COL_PHOTO_URL_TYPE = "TEXT",

  /** Path to a photo on the local filesystem. Not part of the spec. */
  PERSON_COL_PHOTO_PATH = "photo_path",
  PERSON_COL_PHOTO_PATH_TYPE = "TEXT",

  PERSON_COL_PROFILE_URLS = "profile_urls",
  PERSON_COL_PROFILE_URLS_TYPE = "TEXT",

  /** Whether the router has seen the item. Not part of the spec. */
  PERSON_COL_ROUTED = "routed",
  PERSON_COL_ROUTED_TYPE = "INTEGER";


  static final String NOTE_TABLE_NAME = "NOTE";

  static final String
  NOTE_COL_RECORD_ID = "note_record_id",
  NOTE_COL_RECORD_ID_TYPE = "TEXT",

  NOTE_COL_PERSON_ID = "person_record_id",
  NOTE_COL_PERSON_ID_TYPE = "TEXT",

  NOTE_COL_LNK_PERSON_ID = "linked_person_record_id",
  NOTE_COL_LNK_PERSON_ID_TYPE = "TEXT",

  NOTE_COL_ENTRY_DATE = "entry_date",
  NOTE_COL_ENTRY_DATE_TYPE = "INTEGER",

  NOTE_COL_AUTHOR_NAME = "author_name",
  NOTE_COL_AUTHOR_NAME_TYPE = "TEXT",

  NOTE_COL_AUTHOR_EMAIL = "author_email",
  NOTE_COL_AUTHOR_EMAIL_TYPE = "TEXT",

  NOTE_COL_AUTHOR_PHONE = "author_phone",
  NOTE_COL_AUTHOR_PHONE_TYPE = "TEXT",

  NOTE_COL_SOURCE_DATE = "source_date",
  NOTE_COL_SOURCE_DATE_TYPE = "INTEGER",

  NOTE_COL_CONTACT = "author_made_contact",
  NOTE_COL_CONTACT_TYPE = "TEXT",

  NOTE_COL_STATUS = "status",
  NOTE_COL_STATUS_TYPE = "TEXT",

  NOTE_COL_PERSON_EMAIL = "email_of_found_person",
  NOTE_COL_PERSON_EMAIL_TYPE = "TEXT",

  NOTE_COL_PERSON_PHONE = "phone_of_found_person",
  NOTE_COL_PERSON_PHONE_TYPE = "TEXT",

  NOTE_COL_LAST_LOCATION = "last_known_location",
  NOTE_COL_LAST_LOCATION_TYPE = "TEXT",

  NOTE_COL_TEXT = "text",
  NOTE_COL_TEXT_TYPE = "TEXT",

  NOTE_COL_PHOTO_URL = "photo_url",
  NOTE_COL_PHOTO_URL_TYPE = "TEXT",

 /** Path to a photo on the local filesystem. Not part of the spec. */
  NOTE_COL_PHOTO_PATH = "photo_path",
  NOTE_COL_PHOTO_PATH_TYPE = "TEXT",

 /** Whether the router has seen the item. Not part of the spec. */
  NOTE_COL_ROUTED = "routed",
  NOTE_COL_ROUTED_TYPE = "INTEGER";


  //-------------------------------------------------------------------------//
  // Queries
  //-------------------------------------------------------------------------//
  /** SQLite query to create the PERSON database table: {@value} */
   static final String SQL_CREATE_PERSON_TABLE
      = "CREATE TABLE " + PERSON_TABLE_NAME +
      " (" +
      PERSON_COL_RECORD_ID + " " + PERSON_COL_RECORD_ID_TYPE +
      " PRIMARY KEY ON CONFLICT IGNORE NOT NULL, " +
      PERSON_COL_ENTRY_DATE + " " + PERSON_COL_ENTRY_DATE_TYPE + ", " +
      PERSON_COL_EXPIRY_DATE + " " + PERSON_COL_EXPIRY_DATE_TYPE + ", " +
      PERSON_COL_AUTHOR_NAME + " " + PERSON_COL_AUTHOR_NAME_TYPE + ", " +
      PERSON_COL_AUTHOR_EMAIL + " " + PERSON_COL_AUTHOR_EMAIL_TYPE + ", " +
      PERSON_COL_AUTHOR_PHONE + " " + PERSON_COL_AUTHOR_PHONE_TYPE + ", " +
      PERSON_COL_SOURCE_NAME + " " + PERSON_COL_SOURCE_NAME_TYPE + ", " +
      PERSON_COL_SOURCE_DATE + " " + PERSON_COL_SOURCE_DATE_TYPE + ", " +
      PERSON_COL_SOURCE_URL + " " + PERSON_COL_SOURCE_URL_TYPE + ", " +
      PERSON_COL_FULL_NAME + " " + PERSON_COL_FULL_NAME_TYPE + ", " +
      PERSON_COL_GIVEN_NAME + " " + PERSON_COL_GIVEN_NAME_TYPE + ", " +
      PERSON_COL_FAMILY_NAME + " " + PERSON_COL_FAMILY_NAME_TYPE + ", " +
      PERSON_COL_ALT_NAMES + " " + PERSON_COL_ALT_NAMES_TYPE + ", " +
      PERSON_COL_DESCRIPTION + " " + PERSON_COL_DESCRIPTION_TYPE + ", " +
      PERSON_COL_SEX + " " + PERSON_COL_SEX_TYPE + ", " +
      PERSON_COL_DOB + " " + PERSON_COL_DOB_TYPE + ", " +
      PERSON_COL_AGE + " " + PERSON_COL_AGE_TYPE + ", " +
      PERSON_COL_HOME_STREET + " " + PERSON_COL_HOME_STREET_TYPE + ", " +
      PERSON_COL_HOME_NGHBRHD + " " + PERSON_COL_HOME_NGHBRHD_TYPE + ", " +
      PERSON_COL_HOME_CITY + " " + PERSON_COL_HOME_CITY_TYPE + ", " +
      PERSON_COL_HOME_STATE + " " + PERSON_COL_HOME_STATE_TYPE + ", " +
      PERSON_COL_HOME_ZIP + " " + PERSON_COL_HOME_ZIP_TYPE + ", " +
      PERSON_COL_HOME_COUNTRY + " " + PERSON_COL_HOME_COUNTRY_TYPE + ", " +
      PERSON_COL_PHOTO_URL + " " + PERSON_COL_PHOTO_URL_TYPE + ", " +
      PERSON_COL_PHOTO_PATH + " " + PERSON_COL_PHOTO_PATH_TYPE + ", " +
      PERSON_COL_PROFILE_URLS + " " + PERSON_COL_PROFILE_URLS_TYPE + ", " +
      PERSON_COL_ROUTED + " " + PERSON_COL_ROUTED_TYPE +
      ");";

  /** SQLite query to create the NOTE database table: {@value} */
   static final String SQL_CREATE_NOTE_TABLE
      = "CREATE TABLE " + NOTE_TABLE_NAME +
      " (" +
      NOTE_COL_RECORD_ID + " " + NOTE_COL_RECORD_ID_TYPE + " " +
      "PRIMARY KEY ON CONFLICT IGNORE NOT NULL, " +
      NOTE_COL_PERSON_ID + " " + NOTE_COL_PERSON_ID_TYPE + " " +
      "NOT NULL REFERENCES " +
      PERSON_TABLE_NAME + "(" + PERSON_COL_RECORD_ID + "), " +
      NOTE_COL_LNK_PERSON_ID + " " + NOTE_COL_LNK_PERSON_ID_TYPE + " " +
      "REFERENCES " +
      PERSON_TABLE_NAME + "(" + PERSON_COL_RECORD_ID + "), " +
      NOTE_COL_ENTRY_DATE + " " + NOTE_COL_ENTRY_DATE_TYPE + ", " +
      NOTE_COL_AUTHOR_NAME + " " + NOTE_COL_AUTHOR_NAME_TYPE + ", " +
      NOTE_COL_AUTHOR_EMAIL + " " + NOTE_COL_AUTHOR_EMAIL_TYPE + ", " +
      NOTE_COL_AUTHOR_PHONE + " " + NOTE_COL_AUTHOR_PHONE_TYPE + ", " +
      NOTE_COL_SOURCE_DATE + " " + NOTE_COL_SOURCE_DATE_TYPE + ", " +
      NOTE_COL_CONTACT + " " + NOTE_COL_CONTACT_TYPE + ", " +
      NOTE_COL_STATUS + " " + NOTE_COL_STATUS_TYPE + ", " +
      NOTE_COL_PERSON_EMAIL + " " + NOTE_COL_PERSON_EMAIL_TYPE + ", " +
      NOTE_COL_PERSON_PHONE + " " + NOTE_COL_PERSON_PHONE_TYPE + ", " +
      NOTE_COL_LAST_LOCATION + " " + NOTE_COL_LAST_LOCATION_TYPE + ", " +
      NOTE_COL_TEXT + " " + NOTE_COL_TEXT_TYPE + ", " +
      NOTE_COL_PHOTO_URL + " " + NOTE_COL_PHOTO_URL_TYPE + ", " +
      NOTE_COL_PHOTO_PATH + " " + NOTE_COL_PHOTO_PATH_TYPE + ", " +
      NOTE_COL_ROUTED + " " + NOTE_COL_ROUTED_TYPE +
      ");";

   static final String SQL_SELECT_PERSONS =
      "SELECT " +
          PERSON_COL_RECORD_ID + ", " +
          PERSON_COL_FULL_NAME + " " +
          "FROM " +
          PERSON_TABLE_NAME + " " +
          "WHERE " +
          PERSON_COL_FULL_NAME + " LIKE '%[NAME]%'";

   static final String SQL_SELECT_UNROUTED_PERSONS =
      "SELECT " +
          "*" + " " +
          "FROM " +
          PERSON_TABLE_NAME + " " +
          "WHERE " +
          PERSON_COL_ROUTED + " = 0";

   static final String SQL_SELECT_ALL_PERSONS =
      "SELECT " +
          PERSON_COL_RECORD_ID + ", " +
          PERSON_COL_FULL_NAME + " " +
          "FROM " +
          PERSON_TABLE_NAME + " " +
          "ORDER BY " +
          PERSON_COL_GIVEN_NAME;

   static final String SQL_SELECT_PERSON_DETAILS =
      "SELECT " +
          "*" + " " +
          "FROM " +
          PERSON_TABLE_NAME + " " +
          "WHERE " +
          PERSON_COL_RECORD_ID + " = '[PERSON_ID]'";


  /**
   * Greatest-n-per-group query following: https://stackoverflow
   * .com/questions/2111384/ sql-join-selecting-the-last-records-in-a-one-to
   * -many-relationship
   * <p/>
   * This is necessary since we want to return the latest status of the person
   * from the notes table along with the person's other details.
   */
   static final String SQL_SELECT_ALL_PERSONS_WITH_STATUS =
      "SELECT " +
          "c." + PERSON_COL_RECORD_ID + ", " +
          "c." + PERSON_COL_FULL_NAME + ", " +
          "p1." + NOTE_COL_STATUS + ", " +
          "c." + PERSON_COL_ENTRY_DATE + " " +
          "FROM " + PERSON_TABLE_NAME + " c" + " " +
          "LEFT JOIN " +
          NOTE_TABLE_NAME + " p1 " +
          "ON " +
          "(c." + PERSON_COL_RECORD_ID + " = p1." + NOTE_COL_PERSON_ID + ") " +
          "LEFT JOIN " +
          NOTE_TABLE_NAME + " p2 " +
          "ON " +
          "(c." + PERSON_COL_RECORD_ID + " = p2." + NOTE_COL_PERSON_ID + " " +
          "AND " +
          "(p1." + NOTE_COL_ENTRY_DATE + " < p2." + NOTE_COL_ENTRY_DATE + " " +
          "OR " +
          "p1." + NOTE_COL_ENTRY_DATE + " = p2." + NOTE_COL_ENTRY_DATE + " " +
          "AND " +
          "p1." + NOTE_COL_RECORD_ID + " < p2." + NOTE_COL_RECORD_ID + ")" +
          ") " +
          "WHERE p2." + NOTE_COL_PERSON_ID + " IS NULL" + " " +
          "ORDER BY c." + PERSON_COL_GIVEN_NAME;

   static final String SQL_SELECT_NOTES_FOR_PERSON =
      "SELECT " +
          "*" + " " +
          "FROM " +
          NOTE_TABLE_NAME + " " +
          "WHERE " +
          NOTE_COL_PERSON_ID + " = '[PERSON_ID]'";

   static final String SQL_SELECT_UNROUTED_NOTES =
      "SELECT " +
          "*" + " " +
          "FROM " +
          NOTE_TABLE_NAME + " " +
          "WHERE " +
          NOTE_COL_ROUTED + " = 0";

  /** SQLite query for updating the routed status of a Person. */
   static final String SQL_UPDATE_PERSON_ROUTED =
      "UPDATE " +
          PERSON_TABLE_NAME + " " +
          "SET " +
          PERSON_COL_ROUTED + " = 1 " +
          "WHERE " +
          PERSON_COL_RECORD_ID + " = '[PERSON_ID]'";

  /** SQLite query for updating the routed status of a Note. */
  static final String SQL_UPDATE_NOTE_ROUTED =
      "UPDATE " +
          NOTE_TABLE_NAME + " " +
          "SET " +
          NOTE_COL_ROUTED + " = 1 " +
          "WHERE " +
          NOTE_COL_RECORD_ID + " = '[NOTE_ID]'";
  //=========================================================================//

}
