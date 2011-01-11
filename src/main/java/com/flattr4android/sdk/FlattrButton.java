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

import com.flattr4android.rest.FlattrRestClient;
import com.flattr4android.rest.Thing;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * A <code>FlattrButton</code> represents a Flattr button. It embeds all the
 * necessary code and behavior to be useful as it is. It mostly requires setup
 * in the layout file.
 * 
 * @see http://flattr4android.com/sdk/setup.php
 * 
 * @author Philippe Bernard
 */
public class FlattrButton extends View {

	public static final String BUTTON_STYLE_VERTICAL = "vertical";
	public static final String BUTTON_STYLE_HORIZONTAL = "horizontal";

	public static final int CLICK_TEXT_COLOR = 0xff000000;

	private boolean verticalResIntialized = false;
	private Drawable buttonTop;
	private int buttonTopWidth, buttonTopHeight;
	private Drawable buttonVMiddle;
	private int buttonVMiddleWidth, buttonVMiddleHeight;
	private Drawable buttonBottomFlattr, buttonBottomFlattred,
			buttonBottomMyThing, buttonBottomInactive;
	private int buttonBottomWidth, buttonBottomHeight;

	private TextPaint verticalClickPaint;
	private float verticalClickTextHeight;

	private boolean horizontalResIntialized = false;
	private Drawable buttonLeftFlattr, buttonLeftFlattred, buttonLeftMyThing,
			buttonLeftInactive;
	private int buttonLeftWidth, buttonLeftHeight;
	private Drawable buttonHMiddle;
	private int buttonHMiddleWidth, buttonHMiddleHeight;
	private Drawable buttonRight;
	private int buttonRightWidth, buttonRightHeight;

	private TextPaint horizontalClickPaint;
	private float horizontalClickTextHeight;

	private String style = BUTTON_STYLE_HORIZONTAL;

	private FlattrRestClient flattrClient;
	private String thingId;
	private int thingStatus;
	private int thingClicks;
	private boolean thingSet = false;
	private boolean thingGotAsUser;
	private Exception thingError;

	public FlattrButton(Context context) throws FlattrSDKException {
		super(context);
		initResources();
		initListener();
	}

	public FlattrButton(Context context, AttributeSet attrs)
			throws FlattrSDKException {
		super(context, attrs);

		setThingId(getAttribute(attrs, "thing_id"));

		setFlattrCredentials(getAttribute(attrs, "token"),
				getAttribute(attrs, "token_secret"));

		String style = getAttribute(attrs, "button_style", false);
		if (style != null) {
			setButtonStyle(style);
		}
		initResources();
		initListener();
	}

	private void initListener() {
		setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				try {
					FlattrSDK.displayThing(getContext(), thingId);
				} catch (FlattrSDKException e) {
					Log.d(FlattrSDK.LOG_TAG, "Error while displaying thing "
							+ thingId, e);
				}
			}
		});
	}

	/**
	 * Set Oauth credentials, got from <a
	 * href="http://flattr4android.com/sdk/">Flattr4Android.com</a>.
	 */
	public void setFlattrCredentials(String token, String tokenSecret) {
		flattrClient = new FlattrRestClient(FlattrSDK.CONSUMER_KEY,
				FlattrSDK.CONSUMER_SECRET, token, tokenSecret);
		if (thingId != null) {
			new ThingLoader(flattrClient, thingId).execute();
		}
	}

	/**
	 * Set targeted thing Id, got from <a
	 * href="http://flattr4android.com/sdk/">Flattr4Android.com</a> or the
	 * Flattr Rest API.
	 */
	public void setThingId(String thingId) {
		this.thingId = thingId;
		if (flattrClient != null) {
			new ThingLoader(flattrClient, thingId).execute();
		}
	}

	public String getThingId() {
		return thingId;
	}

	/**
	 * @see FlattrButton#BUTTON_STYLE_HORIZONTAL
	 * @see FlattrButton#BUTTON_STYLE_VERTICAL
	 */
	public void setButtonStyle(String style) throws FlattrSDKException {
		if ((!style.equals(BUTTON_STYLE_HORIZONTAL))
				&& (!style.equals(BUTTON_STYLE_VERTICAL))) {
			throw new IllegalArgumentException("Invalid style '" + style
					+ "' (only " + BUTTON_STYLE_HORIZONTAL + " and "
					+ BUTTON_STYLE_VERTICAL + " are allowed)");
		}
		this.style = style;
		initResources();
		invalidate();
	}

	public String getButtonStyle() {
		return style;
	}

	private String getAttribute(AttributeSet attrs, String attrName)
			throws FlattrSDKException {
		return getAttribute(attrs, attrName, true);
	}

	private String getAttribute(AttributeSet attrs, String attrName,
			boolean mandatory) throws FlattrSDKException {
		String value = attrs.getAttributeValue(
				FlattrSDK.FLATTR_SDK_XML_NAMESPACE, attrName);
		if (mandatory && (value == null)) {
			throw new FlattrSDKException("Cannot find attribute '" + attrName
					+ "'. Please make sure you set it with the namespace '"
					+ FlattrSDK.FLATTR_SDK_XML_NAMESPACE + "'");
		}
		return value;
	}

	private void initResources() throws FlattrSDKException {
		Bitmap tmp;

		if (style.equals(BUTTON_STYLE_HORIZONTAL)) {
			if (verticalResIntialized) {
				// Clear reference to allow garbage collection
				verticalResIntialized = false;
				buttonTop = null;
				buttonVMiddle = null;
				buttonBottomFlattr = null;
				buttonBottomFlattred = null;
				buttonBottomMyThing = null;
				buttonBottomInactive = null;

				verticalClickPaint = null;
			}
			if (!horizontalResIntialized) {
				buttonLeftFlattr = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_LEFT_FLATTR,
												"drawable", getContext()));
				((BitmapDrawable) buttonLeftFlattr).setAntiAlias(true);
				buttonLeftFlattred = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_LEFT_FLATTRED,
												"drawable", getContext()));
				((BitmapDrawable) buttonLeftFlattred).setAntiAlias(true);
				buttonLeftMyThing = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_LEFT_MYTHING,
												"drawable", getContext()));
				((BitmapDrawable) buttonLeftMyThing).setAntiAlias(true);
				buttonLeftInactive = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_LEFT_INACTIVE,
												"drawable", getContext()));
				((BitmapDrawable) buttonLeftInactive).setAntiAlias(true);
				tmp = ((BitmapDrawable) buttonLeftFlattr).getBitmap();
				buttonLeftWidth = tmp.getWidth();
				buttonLeftHeight = tmp.getHeight();

				buttonHMiddle = getResources().getDrawable(
						FlattrSDK.getResourceId(
								FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_MIDDLE,
								"drawable", getContext()));
				((BitmapDrawable) buttonHMiddle).setAntiAlias(true);
				tmp = ((BitmapDrawable) buttonHMiddle).getBitmap();
				buttonHMiddleWidth = tmp.getWidth();
				buttonHMiddleHeight = tmp.getHeight();

				buttonRight = getResources().getDrawable(
						FlattrSDK.getResourceId(
								FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_RIGHT,
								"drawable", getContext()));
				((BitmapDrawable) buttonRight).setAntiAlias(true);
				tmp = ((BitmapDrawable) buttonRight).getBitmap();
				buttonRightWidth = tmp.getWidth();
				buttonRightHeight = tmp.getHeight();

				FontMetrics metrics;

				horizontalClickPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
				horizontalClickPaint.setColor(CLICK_TEXT_COLOR);
				horizontalClickPaint.setTextAlign(Paint.Align.CENTER);
				horizontalClickPaint.setTextSize(buttonRightHeight / 2f);
				metrics = horizontalClickPaint.getFontMetrics();
				horizontalClickTextHeight = metrics.ascent + metrics.descent;

				horizontalResIntialized = true;
			}
		} else if (style.equals(BUTTON_STYLE_VERTICAL)) {
			if (horizontalResIntialized) {
				// Clear reference to allow garbage collection
				horizontalResIntialized = false;
				buttonLeftFlattr = null;
				buttonLeftFlattred = null;
				buttonLeftMyThing = null;
				buttonLeftInactive = null;
				buttonHMiddle = null;
				buttonRight = null;

				horizontalClickPaint = null;
			}
			if (!verticalResIntialized) {
				buttonTop = getResources().getDrawable(
						FlattrSDK.getResourceId(
								FlattrSDK.RESOURCE_BUTTON_VERTICAL_TOP,
								"drawable", getContext()));
				tmp = ((BitmapDrawable) buttonTop).getBitmap();
				buttonTopWidth = tmp.getWidth();
				buttonTopHeight = tmp.getHeight();

				buttonVMiddle = getResources().getDrawable(
						FlattrSDK.getResourceId(
								FlattrSDK.RESOURCE_BUTTON_VERTICAL_MIDDLE,
								"drawable", getContext()));
				tmp = ((BitmapDrawable) buttonVMiddle).getBitmap();
				buttonVMiddleWidth = tmp.getWidth();
				buttonVMiddleHeight = tmp.getHeight();

				buttonBottomFlattr = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_VERTICAL_BOTTOM_FLATTR,
												"drawable", getContext()));
				((BitmapDrawable) buttonBottomFlattr).setAntiAlias(true);
				buttonBottomFlattred = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_VERTICAL_BOTTOM_FLATTRED,
												"drawable", getContext()));
				((BitmapDrawable) buttonBottomFlattred).setAntiAlias(true);
				buttonBottomMyThing = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_VERTICAL_BOTTOM_MYTHING,
												"drawable", getContext()));
				((BitmapDrawable) buttonBottomMyThing).setAntiAlias(true);
				buttonBottomInactive = getResources()
						.getDrawable(
								FlattrSDK
										.getResourceId(
												FlattrSDK.RESOURCE_BUTTON_VERTICAL_BOTTOM_INACTIVE,
												"drawable", getContext()));
				((BitmapDrawable) buttonBottomInactive).setAntiAlias(true);
				tmp = ((BitmapDrawable) buttonBottomFlattr).getBitmap();
				buttonBottomWidth = tmp.getWidth();
				buttonBottomHeight = tmp.getHeight();

				FontMetrics metrics;

				verticalClickPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
				verticalClickPaint.setColor(CLICK_TEXT_COLOR);
				verticalClickPaint.setTextAlign(Paint.Align.CENTER);
				verticalClickPaint.setTextSize(buttonTopHeight / 3f);
				metrics = verticalClickPaint.getFontMetrics();
				verticalClickTextHeight = metrics.ascent + metrics.descent;

				verticalResIntialized = true;
			}
		}
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

		switch (MeasureSpec.getMode(widthMeasureSpec)) {
		case (MeasureSpec.EXACTLY):
			break;
		case (MeasureSpec.AT_MOST):
			// "At most" must sometimes be infringed. For example, expected
			// height is sometimes 0... not very convenient.
		case (MeasureSpec.UNSPECIFIED):
			if (style.equals(BUTTON_STYLE_VERTICAL)) {
				measuredWidth = buttonTopWidth;
				measuredHeight = buttonTopHeight + buttonVMiddleHeight
						+ buttonBottomHeight;
			} else {
				measuredWidth = buttonLeftWidth + buttonHMiddleWidth
						+ buttonRightWidth;
				measuredHeight = buttonLeftHeight;
			}
			break;
		}
		Log.d(FlattrSDK.LOG_TAG, "Button dimensions: " + measuredWidth + " x "
				+ measuredHeight);
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (style.equals(BUTTON_STYLE_VERTICAL)) {
			buttonTop.setBounds(0, 0, buttonTopWidth, buttonTopHeight);
			buttonTop.draw(canvas);

			buttonVMiddle.setBounds(0, buttonTopHeight, buttonVMiddleWidth,
					buttonTopHeight + buttonVMiddleHeight);
			buttonVMiddle.draw(canvas);

			Drawable buttonBottom;
			switch (getThingStatus()) {
			case (Thing.INT_STATUS_CLICKED):
				buttonBottom = buttonBottomFlattred;
				break;
			case (Thing.INT_STATUS_INACTIVE):
				buttonBottom = buttonBottomInactive;
				break;
			case (Thing.INT_STATUS_OK):
				buttonBottom = buttonBottomFlattr;
				break;
			case (Thing.INT_STATUS_OWNER):
				buttonBottom = buttonBottomMyThing;
				break;
			default:
				// Other cases: display a regular button
				buttonBottom = buttonBottomFlattr;
				break;
			}
			buttonBottom.setBounds(0, buttonTopHeight + buttonVMiddleHeight,
					buttonBottomWidth, buttonTopHeight + buttonVMiddleHeight
							+ buttonBottomHeight);
			buttonBottom.draw(canvas);

			if (thingSet) {
				drawVerticalClick(canvas, Integer.toString(thingClicks));
			} else if (thingError != null) {
				Log.d(FlattrSDK.LOG_TAG, "Error during thing loading",
						(Exception) thingError);
				drawVerticalClick(canvas, "!");
			} else {
				// The thing is being loaded
				drawVerticalClick(canvas, "?");
			}
		} else {
			Drawable buttonLeft;
			switch (getThingStatus()) {
			case (Thing.INT_STATUS_CLICKED):
				buttonLeft = buttonLeftFlattred;
				break;
			case (Thing.INT_STATUS_INACTIVE):
				buttonLeft = buttonLeftInactive;
				break;
			case (Thing.INT_STATUS_OK):
				buttonLeft = buttonLeftFlattr;
				break;
			case (Thing.INT_STATUS_OWNER):
				buttonLeft = buttonLeftMyThing;
				break;
			default:
				// Other cases: display a regular button
				buttonLeft = buttonLeftFlattr;
				break;
			}
			buttonLeft.setBounds(0, 0, buttonLeftWidth, buttonLeftHeight);
			buttonLeft.draw(canvas);

			buttonHMiddle.setBounds(buttonLeftWidth, 0, buttonLeftWidth
					+ buttonHMiddleWidth, buttonHMiddleHeight);
			buttonHMiddle.draw(canvas);

			buttonRight.setBounds(buttonLeftWidth + buttonHMiddleWidth, 0,
					buttonLeftWidth + buttonHMiddleWidth + buttonRightWidth,
					buttonRightHeight);
			buttonRight.draw(canvas);

			if (thingSet) {
				drawHorizontalClick(canvas, Integer.toString(thingClicks));
			} else if (thingError != null) {
				drawHorizontalClick(canvas, "!");
			} else {
				// The thing is being loaded
				drawHorizontalClick(canvas, "?");
			}
		}
	}

	public int getThingStatus() {
		if (thingSet && thingGotAsUser) {
			return thingStatus;
		} else {
			// As long as we don't know the real status, display a default
			// button
			return Thing.INT_STATUS_OK;
		}
	}

	private void drawVerticalClick(Canvas canvas, String text) {
		canvas.drawText(text, buttonTopWidth / 2f,
				(buttonTopHeight - verticalClickTextHeight) / 2f,
				verticalClickPaint);
	}

	private void drawHorizontalClick(Canvas canvas, String text) {
		canvas.drawText(text, buttonLeftWidth + buttonHMiddleWidth
				+ (buttonRightWidth / 2f),
				(buttonRightHeight - horizontalClickTextHeight) / 2f,
				horizontalClickPaint);
	}

	class ThingLoader extends AsyncTask<Void, Void, Void> {

		private FlattrRestClient flattrClient;
		private String thingId;

		public ThingLoader(FlattrRestClient flattrClient, String thingId) {
			this.flattrClient = flattrClient;
			this.thingId = thingId;

			// Invalidate the current status, if any
			FlattrButton.this.thingSet = false;
			FlattrButton.this.thingError = null;
			FlattrButton.this.invalidate();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// First plan: get the thing through the app
				try {
					ContentResolver cr = getContext().getContentResolver();
					Cursor c = cr.query(
							Uri.parse(FlattrSDK.FLATTR_PROVIDER_CONTENT_URI
									+ "thing/id/" + thingId), null, null, null,
							null);
					if ((c != null) && (c.moveToFirst())) {
						FlattrButton.this.thingStatus = c.getInt(c
								.getColumnIndex("int_status"));
						FlattrButton.this.thingClicks = c.getInt(c
								.getColumnIndex("clicks"));
						// Thing obtained with the user credentials (ie. the
						// Flattr app)
						FlattrButton.this.thingGotAsUser = true;
						FlattrButton.this.thingSet = true;
					}
				} catch (Exception e) {
					FlattrButton.this.thingError = e;
				}

				// Second plan: get the thing with local means
				if ((!FlattrButton.this.thingSet)
						&& (FlattrButton.this.thingError == null)) {
					Thing thing = flattrClient.getThing(thingId);
					FlattrButton.this.thingStatus = thing.getIntStatus();
					FlattrButton.this.thingClicks = thing.getClicks();
					// Thing obtained with the app credentials
					FlattrButton.this.thingGotAsUser = false;
					FlattrButton.this.thingSet = true;
				}
			} catch (Exception e) {
				Log.d(FlattrSDK.LOG_TAG,
						"Error while loading thing " + thingId, e);
				FlattrButton.this.thingError = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			FlattrButton.this.invalidate();
		}
	};

}
