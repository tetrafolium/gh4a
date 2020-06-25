package com.gh4a.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.meisolsson.githubsdk.model.NotificationThread;
import com.meisolsson.githubsdk.model.Repository;

public class NotificationHolder {
@Nullable
public final NotificationThread notification;

@NonNull
public final Repository repository;

private boolean mLastRepositoryNotification;
private boolean mRead;

public NotificationHolder(final @NonNull Repository repository) {
	notification = null;
	this.repository = repository;
}

public NotificationHolder(final @NonNull NotificationThread notification) {
	this.notification = notification;
	repository = notification.repository();
	mRead = !notification.unread();
}

public boolean isLastRepositoryNotification() {
	return mLastRepositoryNotification;
}

public void setIsLastRepositoryNotification(final boolean value) {
	mLastRepositoryNotification = value;
}

public boolean isRead() {
	return mRead;
}

public void setIsRead(final boolean value) {
	mRead = value;
}
}
