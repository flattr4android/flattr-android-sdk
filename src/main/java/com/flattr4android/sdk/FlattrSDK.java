/**
 * Flattr Android SDK
 *
 * Copyright (C) 2010 Flattr4Android - Philippe Bernard
 *   http://flattr4android.com
 *   
 * This software is the confidential and proprietary information of
 * Flattr4Android ("Confidential Information"). You shall not
 * disclose modify or reproduce such Confidential Information unless
 * separate appropriate license rights are granted by Flattr4Android
 * and shall use it only in accordance with the terms of the
 * license agreement you entered into with Flattr4Android.
 * 
 * DISCLAIMER OF WARRANTY:
 * 
 * THIS DOCUMENT IS PROVIDED "AS IS" AND ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE DISCLAIMED, EXCEPT TO THE EXTENT THAT
 * SUCH DISCLAIMERS ARE HELD TO BE LEGALLY INVALID. FLATTR4ANDROID
 * SHALL NOT BE LIABLE FOR ANY SPECIAL, INCIDENTAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OF ANY KIND, ARISING OUT OF OR IN CONNECTION
 * WITH THE USE OF THIS SOFTWARE OR ITS DERIVATIVES.
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

	public static final String FLATTR_THING_BASE_URI = "http://flattr.com/thing/";
	public static final String FLATTR_SDK_XML_NAMESPACE = "http://schemas.flattr4android.com/sdk";

	public static final String LOG_TAG = "FlattrSdk";

	public static final String RESOURCE_PREFIX = "flattr_sdk_";

	public static final String RESOURCE_BUTTON_HORIZONTAL_LEFT = RESOURCE_PREFIX
			+ "button_horizontal_left";
	public static final String RESOURCE_BUTTON_HORIZONTAL_MIDDLE = RESOURCE_PREFIX
			+ "button_horizontal_middle";
	public static final String RESOURCE_BUTTON_HORIZONTAL_RIGHT = RESOURCE_PREFIX
			+ "button_horizontal_right";
	public static final String RESOURCE_BUTTON_VERTICAL_TOP = RESOURCE_PREFIX
			+ "button_vertical_top";
	public static final String RESOURCE_BUTTON_VERTICAL_MIDDLE = RESOURCE_PREFIX
			+ "button_vertical_middle";
	public static final String RESOURCE_BUTTON_VERTICAL_BOTTOM = RESOURCE_PREFIX
			+ "button_vertical_bottom";

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
	 * Return a string got from the XML resources (string.xml). If the string is not found, 
	 * the default value is returned.
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

}
