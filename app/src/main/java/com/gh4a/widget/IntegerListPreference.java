package com.gh4a.widget;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

public class IntegerListPreference extends ListPreference {
public IntegerListPreference(final Context context) {
	super(context);
}

public IntegerListPreference(final Context context, final AttributeSet attrs) {
	super(context, attrs);
}

@Override
protected boolean persistString(final String value) {
	if (value == null) {
		return false;
	}
	return persistInt(Integer.valueOf(value));
}

@Override
protected String getPersistedString(final String defaultReturnValue) {
	if (!getSharedPreferences().contains(getKey())) {
		return defaultReturnValue;
	}
	return String.valueOf(getPersistedInt(0));
}
}
