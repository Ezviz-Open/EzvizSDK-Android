/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.videogo.scan.main;

import android.provider.ContactsContract;

public final class Contents {
  private Contents() {
  }

  public static final class Type {
    public static final String TEXT = "TEXT_TYPE";

    public static final String EMAIL = "EMAIL_TYPE";

    public static final String PHONE = "PHONE_TYPE";

    public static final String SMS = "SMS_TYPE";

    public static final String CONTACT = "CONTACT_TYPE";

    public static final String LOCATION = "LOCATION_TYPE";

    private Type() {
    }
  }

  public static final String URL_KEY = "URL_KEY";

  public static final String NOTE_KEY = "NOTE_KEY";

  public static final String[] PHONE_KEYS = {
      ContactsContract.Intents.Insert.PHONE,
      ContactsContract.Intents.Insert.SECONDARY_PHONE,
      ContactsContract.Intents.Insert.TERTIARY_PHONE
  };

  public static final String[] PHONE_TYPE_KEYS = {
      ContactsContract.Intents.Insert.PHONE_TYPE,
      ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
      ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE
  };

  public static final String[] EMAIL_KEYS = {
      ContactsContract.Intents.Insert.EMAIL,
      ContactsContract.Intents.Insert.SECONDARY_EMAIL,
      ContactsContract.Intents.Insert.TERTIARY_EMAIL
  };

  public static final String[] EMAIL_TYPE_KEYS = {
      ContactsContract.Intents.Insert.EMAIL_TYPE,
      ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE,
      ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE
  };

}
