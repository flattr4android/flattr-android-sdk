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

/**
 * @author Philippe Bernard
 */
@SuppressWarnings("serial")
public class FlattrSDKException extends RuntimeException {

	public FlattrSDKException(String msg) {
		super(msg);
	}
}
