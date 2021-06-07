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

import java.io.Serializable;
import java.util.LinkedList;

public final class DataModel {
  private DataModel() {}

  //-------------------------------------------------------------------------//
  // Class definitions
  //-------------------------------------------------------------------------//

  /** Person's name and ID from the database. */
  public static final class PersonName {
    public final String recordID;
    public final String fullName;
    public final String status;
    public final Long entryDate;

    public PersonName( String recordID, String fullName,
                       String status, Long entryDate ) {
      this.recordID = recordID;
      this.fullName = fullName;
      this.status = status;
      this.entryDate = entryDate;
    }
  }

  /**
   * Contains a {@link Person} record and related local state.
   */
  public static final class LocalPerson {
    public final Person person;
    /** Path to a photo in the local filesystem. */
    public final String photoPath;
    /** Whether the router has seen this record. */
    public final Boolean routed;

    public LocalPerson( Person person, String photoPath, Boolean routed ) {
      this.person = person;
      this.photoPath = photoPath;
      this.routed = routed;
    }
  }

  /**
   * Describes a PERSON record as defined in FPIF 1.4.
   */
  public static final class Person
      implements Serializable {
    private static final long serialVersionUID = 4434461306529712901L;
    public final Metadata metadata;
    public final Identity identity;

    public Person(
        // Metadata
        String recordID, Long entryDate, Long expiryDate,
        String authorName, String authorEmail,
        String authorPhone, String sourceName,
        Long sourceDate, String sourceUrl,
        // Identity
        String fullName, String givenName,
        String familyName, String alternateNames,
        String description, String sex, String dateOfBirth, Long age,
        String street, String neighborhood, String city,
        String state, String zip, String country,
        String photoUrl, String profileUrls
    ) {
      this.metadata = new Metadata( recordID, entryDate, expiryDate,
          authorName, authorEmail, authorPhone,
          sourceName, sourceDate, sourceUrl );
      this.identity = new Identity( fullName, givenName, familyName,
          alternateNames, description, sex,
          dateOfBirth, age, street,
          neighborhood, city, state, zip, country,
          photoUrl, profileUrls );
    }

    public static final class Metadata
        implements Serializable {
      private static final long serialVersionUID = 8216894370014742035L;
      public final String recordID;
      public final Long entryDate;
      public final Long expiryDate;
      public final String authorName;
      public final String authorEmail;
      public final String authorPhone;
      public final String sourceName;
      public final Long sourceDate;
      public final String sourceUrl;

      public Metadata( String recordID, Long entryDate, Long expiryDate,
                       String authorName, String authorEmail,
                       String authorPhone, String sourceName,
                       Long sourceDate, String sourceUrl ) {
        this.recordID = recordID;
        this.entryDate = entryDate;
        this.expiryDate = expiryDate;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.authorPhone = authorPhone;
        this.sourceName = sourceName;
        this.sourceDate = sourceDate;
        this.sourceUrl = sourceUrl;
      }
    }

    public static final class Identity
        implements Serializable {
      private static final long serialVersionUID = 7211823938331785634L;
      public final Name name;
      public final String description;
      public final String sex;
      public final String dateOfBirth;
      public final Long age;
      public final Home home;
      public final String photoUrl;
      public final String profileUrls;

      public Identity(
          // Name
          String fullName, String givenName,
          String familyName, String alternateNames,
          // Other
          String description, String sex, String dateOfBirth, Long age,
          // Home
          String street, String neighborhood, String city,
          String state, String zip, String country,
          // Other
          String photoUrl, String profileUrls
      ) {
        this.name = new Name( fullName, givenName, familyName,
            alternateNames );
        this.description = description;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.home = new Home( street, neighborhood, city, state, zip,
            country );
        this.photoUrl = photoUrl;
        this.profileUrls = profileUrls;
      }

      public static final class Name
          implements Serializable {
        private static final long serialVersionUID = 5779460824911017666L;
        public final String fullName;
        public final String givenName;
        public final String familyName;
        public final String alternateNames;

        public Name( String fullName, String givenName,
                     String familyName, String alternateNames ) {
          this.fullName = fullName;
          this.givenName = givenName;
          this.familyName = familyName;
          this.alternateNames = alternateNames;
        }
      }

      public static final class Home
          implements Serializable {
        private static final long serialVersionUID = -4507882159564640662L;
        public final String street;
        public final String neighborhood;
        public final String city;
        public final String state;
        public final String zip;
        public final String country;

        public Home( String street, String neighborhood, String city,
                     String state, String zip, String country ) {
          this.street = street;
          this.neighborhood = neighborhood;
          this.city = city;
          this.state = state;
          this.zip = zip;
          this.country = country;
        }
      }
    }
  }

  /**
   * Contains a {@link Note} record and related local state.
   */
  public static final class LocalNote {
    public final Note note;
    /** Path to a photo in the local filesystem. */
    public final String photoPath;
    /** Whether the note has been seen by the router. */
    public final Boolean routed;

    public LocalNote( Note note, String photoPath, Boolean routed ) {
      this.note = note;
      this.photoPath = photoPath;
      this.routed = routed;
    }
  }

  /**
   * Describes a NOTE record as defined in FPIF 1.4.
   */
  public static final class Note
      implements Serializable {
    private static final long serialVersionUID = 6743093151968076250L;
    public final Metadata metadata;
    public final Status status;

    public Note(
        // Metadata
        String recordID, String personID,
        String linkedPersonID, Long entryDate,
        String authorName, String authorEmail,
        String authorPhone, Long sourceDate,
        // Status
        Boolean authorMadeContact, String status,
        String emailOfFoundPerson, String phoneOfFoundPerson,
        String lastKnownLocation, String text,
        String photoUrl
    ) {
      this.metadata = new Metadata( recordID, personID, linkedPersonID,
          entryDate, authorName, authorEmail,
          authorPhone, sourceDate );
      this.status = new Status( authorMadeContact, status,
          emailOfFoundPerson, phoneOfFoundPerson,
          lastKnownLocation, text, photoUrl );
    }

    public static final class Metadata
        implements Serializable {
      private static final long serialVersionUID = 332809618557625129L;
      public final String recordID;
      public final String personID;
      public final String linkedPersonID;
      public final Long entryDate;
      public final String authorName;
      public final String authorEmail;
      public final String authorPhone;
      public final Long sourceDate;

      public Metadata( String recordID, String personID,
                       String linkedPersonID, Long entryDate,
                       String authorName, String authorEmail,
                       String authorPhone, Long sourceDate ) {
        this.recordID = recordID;
        this.personID = personID;
        this.linkedPersonID = linkedPersonID;
        this.entryDate = entryDate;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.authorPhone = authorPhone;
        this.sourceDate = sourceDate;
      }
    }

    public static final class Status
        implements Serializable {
      private static final long serialVersionUID = -7362519817209434472L;
      public final Boolean authorMadeContact;
      public final String status;
      public final String emailOfFoundPerson;
      public final String phoneOfFoundPerson;
      public final String lastKnownLocation;
      public final String text;
      public final String photoUrl;

      public Status( Boolean authorMadeContact, String status,
                     String emailOfFoundPerson, String phoneOfFoundPerson,
                     String lastKnownLocation, String text,
                     String photoUrl ) {
        this.authorMadeContact = authorMadeContact;
        this.status = status;
        this.emailOfFoundPerson = emailOfFoundPerson;
        this.phoneOfFoundPerson = phoneOfFoundPerson;
        this.lastKnownLocation = lastKnownLocation;
        this.text = text;
        this.photoUrl = photoUrl;
      }
    }
  }

  /**
   * A class used to serialize a person record and all related notes.
   */
  public static final class SerializablePerson
      implements Serializable {
    private static final long serialVersionUID = -8571798185935467571L;
    public final Person person;
    public final LinkedList<Note> notes;

    public SerializablePerson( Person person, LinkedList<Note> notes ) {
      this.person = person;
      this.notes = notes;
    }
  }
  //=========================================================================//
}
