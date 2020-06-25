package com.gh4a.utils;

import android.app.Application;

import com.tspoon.traceur.Traceur;

public class DebuggingHelper {
public static void onCreate(final Application app) {
	Traceur.enableLogging();
}
}
