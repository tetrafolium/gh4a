package com.gh4a.resolver;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.utils.IntentUtils;
import com.gh4a.utils.Optional;
import com.gh4a.utils.UiUtils;

import io.reactivex.Single;

public abstract class UrlLoadTask extends AsyncTask<Void, Void, Optional<Intent>> {
    protected final FragmentActivity mActivity;
    private ProgressDialogFragment mProgressDialog;

    public UrlLoadTask(final FragmentActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialogFragment();
        mProgressDialog.show(mActivity.getSupportFragmentManager(), "progress");
    }

    @Override
    protected Optional<Intent> doInBackground(final Void... params) {
        try {
            return getSingle().blockingGet();
        } catch (Exception e) {
            Log.e(Gh4Application.LOG_TAG, "Failure during intent resolving", e);
            return Optional.absent();
        }
    }

    @Override
    protected void onPostExecute(final Optional<Intent> result) {
        if (mActivity.isFinishing()) {
            return;
        }

        if (result.isPresent()) {
            mActivity.startActivity(result.get());
        } else {
            IntentUtils.launchBrowser(mActivity, mActivity.getIntent().getData());
        }

        if (mProgressDialog != null && mProgressDialog.isAdded()) {
            mProgressDialog.dismissAllowingStateLoss();
        }
        mActivity.finish();
    }

    protected abstract Single<Optional<Intent>> getSingle();

    public static class ProgressDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            return UiUtils.createProgressDialog(getActivity(), R.string.loading_msg);
        }
    }
}
