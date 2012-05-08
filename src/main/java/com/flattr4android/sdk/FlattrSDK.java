/* Copyright (c) 2010-2011 Flattr4Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flattr4android.sdk;

import org.shredzone.flattr4j.model.Thing;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

/**
 * Root SDK class.
 * 
 * @author Philippe Bernard
 */
public class FlattrSDK {

	public static final String FLATTR_APP_PACKAGE = "com.flattr4android.app";
	public static final String FLATTR_APP_INTENT = FLATTR_APP_PACKAGE
			+ ".DISPLAY_THING";
	public static final String THING_PARAMETER = "THING_ID";

	public static final String FLATTR_PROVIDER_AUTHORITY = "com.flattr4android.provider.app";
	public static final String FLATTR_PROVIDER_CONTENT_URI = "content://"
			+ FLATTR_PROVIDER_AUTHORITY + "/";

	public static final String FLATTR_THING_BASE_URI = "http://flattr.com/thing/";
	public static final String FLATTR_SDK_XML_NAMESPACE = "http://schemas.flattr4android.com/sdk";

	public static final String LOG_TAG = "FlattrSdk";

	/**
	 * Present a Flattr thing to the user. This method tries to: - Invoke the
	 * Flattr application, if it's present on the phone. - Else, display a
	 * dialog to let the user choose between going to the Flattr web site or
	 * download the Flattr application.
	 * 
	 * @param thingId
	 *            The thing Id, got from <a href=http://flattr4android.com/sdk">
	 *            Flattr4Android.com</a> or the Rest API. For example,
	 *            <code>"7e4c65bfab8ee31e7d79f4d3b7bcfe19"</code>.
	 */
	public static void displayThing(Context context, String thingId)
			throws FlattrSDKException {
		try {
			context.startActivity(getDisplayThingIntent(thingId));
		} catch (ActivityNotFoundException e) {
			// No Flattr4Android there? Propose some other options
			showDisplayThingDialog(context, thingId);
		}
	}

	/**
	 * Return the intent to launch the Flattr application.
	 */
	public static Intent getDisplayThingIntent(String thingId) {
		Intent intent = new Intent(FlattrSDK.FLATTR_APP_INTENT);
		intent.putExtra(FlattrSDK.THING_PARAMETER, thingId);
		return intent;
	}

	private static void showDisplayThingDialog(final Context context,
			final String thingId) {
		Resources res = context.getResources();
		final CharSequence[] items = {
				res.getString(R.string.install_app),
				res.getString(R.string.go_to_flattr) };

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(res.getString(R.string.choose_option));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Uri uri = null;
				switch (item) {
				case (0):
					// Download the Flattr app
					uri = Uri.parse("market://search?q=pname:"
							+ FLATTR_APP_PACKAGE);
					break;
				case (1):
					// Go to Flattr
					uri = Uri.parse(FLATTR_THING_BASE_URI + thingId);
					break;
				default:
					Log.d(LOG_TAG, "Unexpected case in Display Thing dialog: "
							+ item);
					return;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				context.startActivity(intent);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static ThingStatus getStatus(Thing thing) {
		// TODO: Implement me!
		return ThingStatus.DEFAULT;
	}
}
