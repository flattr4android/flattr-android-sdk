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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Root SDK class.
 * 
 * @author Philippe Bernard
 */
public class FlattrSDK {

	static final String CONSUMER_KEY = "rrdeL1qORFfpByYHXXMsUDtbjkoLf2txkxBWoSA6nGlP4anSX0aHtuJDEYeKQy71";
	static final String CONSUMER_SECRET = "VzNaie6vKiaEion6eoDxId1c15NIuKzfadfihbERkFmsTzoXNSllVcoN81msCLxD";

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

	public static final String RESOURCE_PREFIX = "flattr_sdk_";

	public static final String RESOURCE_BUTTON_HORIZONTAL_LEFT_FLATTR = RESOURCE_PREFIX
			+ "button_horizontal_left_flattr";
	public static final String RESOURCE_BUTTON_HORIZONTAL_LEFT_FLATTRED = RESOURCE_PREFIX
			+ "button_horizontal_left_flattred";
	public static final String RESOURCE_BUTTON_HORIZONTAL_LEFT_MYTHING = RESOURCE_PREFIX
			+ "button_horizontal_left_mything";
	public static final String RESOURCE_BUTTON_HORIZONTAL_LEFT_INACTIVE = RESOURCE_PREFIX
			+ "button_horizontal_left_inactive";
	public static final String RESOURCE_BUTTON_HORIZONTAL_MIDDLE = RESOURCE_PREFIX
			+ "button_horizontal_middle";
	public static final String RESOURCE_BUTTON_HORIZONTAL_RIGHT = RESOURCE_PREFIX
			+ "button_horizontal_right";
	public static final String RESOURCE_BUTTON_VERTICAL_TOP = RESOURCE_PREFIX
			+ "button_vertical_top";
	public static final String RESOURCE_BUTTON_VERTICAL_MIDDLE = RESOURCE_PREFIX
			+ "button_vertical_middle";
	public static final String RESOURCE_BUTTON_VERTICAL_BOTTOM_FLATTR = RESOURCE_PREFIX
			+ "button_vertical_bottom_flattr";
	public static final String RESOURCE_BUTTON_VERTICAL_BOTTOM_FLATTRED = RESOURCE_PREFIX
			+ "button_vertical_bottom_flattred";
	public static final String RESOURCE_BUTTON_VERTICAL_BOTTOM_MYTHING = RESOURCE_PREFIX
			+ "button_vertical_bottom_mything";
	public static final String RESOURCE_BUTTON_VERTICAL_BOTTOM_INACTIVE = RESOURCE_PREFIX
			+ "button_vertical_bottom_inactive";
	public static final String RESOURCE_BUTTON_MINI_LEFT_FLATTR = RESOURCE_PREFIX
			+ "button_mini_left_flattr";
	public static final String RESOURCE_BUTTON_MINI_LEFT_FLATTRED = RESOURCE_PREFIX
			+ "button_mini_left_flattred";
	public static final String RESOURCE_BUTTON_MINI_LEFT_MYTHING = RESOURCE_PREFIX
			+ "button_mini_left_mything";
	public static final String RESOURCE_BUTTON_MINI_LEFT_INACTIVE = RESOURCE_PREFIX
			+ "button_mini_left_inactive";
	public static final String RESOURCE_BUTTON_MINI_MIDDLE = RESOURCE_PREFIX
			+ "button_mini_middle";
	public static final String RESOURCE_BUTTON_MINI_RIGHT = RESOURCE_PREFIX
			+ "button_mini_right";

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
		final CharSequence[] items = {
				getString(RESOURCE_PREFIX + "install_app",
						"Install the Flattr application", context),
				getString(RESOURCE_PREFIX + "go_to_flattr",
						"Go to Flattr Web Site", context) };

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getString(RESOURCE_PREFIX + "choose_option",
				"Choose an option", context));
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

	/**
	 * Return a string got from the XML resources (string.xml). If the string is
	 * not found, the default value is returned.
	 */
	public static String getString(String resId, String defaultValue,
			Context context) {
		try {
			return context.getResources().getString(
					getResourceId(resId, "string", context));
		} catch (FlattrSDKException e) {
			Log.d(LOG_TAG, "Cannot get string '" + resId
					+ "': return the default, English string", e);
			return defaultValue;
		}
	}

	/**
	 * Return a resource id. Usually, this is done through the <code>R</code>
	 * class. As the SDK is integrated in various projetcs, it has to use
	 * reflection instead.
	 * 
	 * @throws FlattrSDKException
	 *             If the resource cannot be found. Probably because the SDK was
	 *             not installed properly.
	 */
	public static int getResourceId(String resId, String type, Context context)
			throws FlattrSDKException {
		int id = context.getResources().getIdentifier(resId, type,
				context.getApplicationContext().getPackageName());
		if (id == 0) {
			throw new FlattrSDKException(
					"Cannot find resource "
							+ type
							+ ":"
							+ resId
							+ ". Please make sure the Flattr SDK was installed properly");
		}
		return id;
	}

	/**
	 * Get an attribute raw value (eg. <code>"Hello"</code> or
	 * <code>"@string/hello"</code>) and resolve its reference, if necessary.
	 * 
	 * @param valueOrRef
	 *            <code>"Hello"</code> or <code>"@string/hello"</code>
	 * @return <code>"Hello"</code> in both cases
	 */
	public static String resolveStringRef(String valueOrRef, Context context) {
		// If the string starts with an "@", this is not a value but a reference
		if (valueOrRef.startsWith("@")) {
			int resId = FlattrSDK.getResourceId(valueOrRef, "string", context);
			valueOrRef = context.getString(resId);
		}
		return valueOrRef;
	}

}
