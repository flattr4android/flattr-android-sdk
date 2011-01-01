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

import com.flattr4android.rest.FlattrRestClient;
import com.flattr4android.rest.Thing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
	private Drawable buttonBottom;
	private int buttonBottomWidth, buttonBottomHeight;

	private TextPaint verticalClickPaint;
	private float verticalClickTextHeight;

	private boolean horizontalResIntialized = false;
	private Drawable buttonLeft;
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
	private Thing thing;
	private Exception thingError;

	public FlattrButton(Context context) throws FlattrSDKException {
		super(context);
		initResources();
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
				buttonBottom = null;

				verticalClickPaint = null;
			}
			if (!horizontalResIntialized) {
				buttonLeft = getResources().getDrawable(
						FlattrSDK.getResourceId(
								FlattrSDK.RESOURCE_BUTTON_HORIZONTAL_LEFT,
								"drawable", getContext()));
				((BitmapDrawable) buttonLeft).setAntiAlias(true);
				tmp = ((BitmapDrawable) buttonLeft).getBitmap();
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
				buttonLeft = null;
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

				buttonBottom = getResources().getDrawable(
						FlattrSDK.getResourceId(
								FlattrSDK.RESOURCE_BUTTON_VERTICAL_BOTTOM,
								"drawable", getContext()));
				tmp = ((BitmapDrawable) buttonBottom).getBitmap();
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

			buttonBottom.setBounds(0, buttonTopHeight + buttonVMiddleHeight,
					buttonBottomWidth, buttonTopHeight + buttonVMiddleHeight
							+ buttonBottomHeight);
			buttonBottom.draw(canvas);

			if (thing != null) {
				drawVerticalClick(canvas, Integer.toString(thing.getClicks()));
			} else if (thingError != null) {
				Log.d(FlattrSDK.LOG_TAG, "Error during thing loading",
						(Exception) thingError);
				drawVerticalClick(canvas, "!");
			} else {
				// The thing is being loaded
				drawVerticalClick(canvas, "?");
			}
		} else {
			buttonLeft.setBounds(0, 0, buttonLeftWidth, buttonLeftHeight);
			buttonLeft.draw(canvas);

			buttonHMiddle.setBounds(buttonLeftWidth, 0, buttonLeftWidth
					+ buttonHMiddleWidth, buttonHMiddleHeight);
			buttonHMiddle.draw(canvas);

			buttonRight.setBounds(buttonLeftWidth + buttonHMiddleWidth, 0,
					buttonLeftWidth + buttonHMiddleWidth + buttonRightWidth,
					buttonRightHeight);
			buttonRight.draw(canvas);

			if (thing != null) {
				drawHorizontalClick(canvas, Integer.toString(thing.getClicks()));
			} else if (thingError != null) {
				drawHorizontalClick(canvas, "!");
			} else {
				// The thing is being loaded
				drawHorizontalClick(canvas, "?");
			}
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

	class ThingLoader extends AsyncTask<Void, Void, Object> {

		private FlattrRestClient flattrClient;
		private String thingId;

		public ThingLoader(FlattrRestClient flattrClient, String thingId) {
			this.flattrClient = flattrClient;
			this.thingId = thingId;

			// Invalidate the current status, if any
			FlattrButton.this.thing = null;
			FlattrButton.this.thingError = null;
			FlattrButton.this.invalidate();
		}

		@Override
		protected Object doInBackground(Void... arg0) {
			try {
				return flattrClient.getThing(thingId);
			} catch (Exception e) {
				Log.d(FlattrSDK.LOG_TAG,
						"Error while loading thing " + thingId, e);
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (result instanceof Thing) {
				FlattrButton.this.thing = (Thing) result;
				FlattrButton.this.thingError = null;
			} else if (result instanceof Exception) {
				FlattrButton.this.thing = null;
				FlattrButton.this.thingError = (Exception) result;
			}
			FlattrButton.this.invalidate();
		}
	};

}
