package com.gh4a.activities.home;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.meisolsson.githubsdk.model.User;

public abstract class FragmentFactory {
    protected final HomeActivity mActivity;

    protected FragmentFactory(final HomeActivity activity) {
        mActivity = activity;
    }

    protected abstract @StringRes int getTitleResId();
    protected abstract int[] getTabTitleResIds();
    protected abstract Fragment makeFragment(int position);

    protected void onFragmentInstantiated(final Fragment f, final int position) {
    }

    protected void onFragmentDestroyed(final Fragment f) {
    }

    protected int[] getHeaderColorAttrs() {
        return null;
    }

    protected int[] getToolDrawerMenuResIds() {
        return null;
    }

    protected void prepareToolDrawerMenu(final Menu menu) {

    }

    protected boolean onDrawerItemSelected(final MenuItem item) {
        return false;
    }

    protected boolean onCreateOptionsMenu(final Menu menu) {
        return false;
    }

    protected boolean onOptionsItemSelected(final MenuItem item) {
        return false;
    }

    protected void onSaveInstanceState(final Bundle outState) { }

    protected void onRestoreInstanceState(final Bundle state) { }

    protected void onRefresh() { }

    protected void onDestroy() { }

    protected @IdRes int getInitialToolDrawerSelection() {
        return 0;
    }

    protected void setUserInfo(final User user) { }

    protected void onStartLoadingData() { }
}
