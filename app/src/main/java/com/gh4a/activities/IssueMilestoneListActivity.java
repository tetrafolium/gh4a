/*
 * Copyright 2011 Azwan Adli Abdullah
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
package com.gh4a.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.gh4a.BaseFragmentPagerActivity;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.fragment.IssueMilestoneListFragment;
import com.gh4a.fragment.LoadingListFragmentBase;

public class IssueMilestoneListActivity extends BaseFragmentPagerActivity
    implements View.OnClickListener,
               LoadingListFragmentBase.OnRecyclerViewCreatedListener {
  public static Intent makeIntent(final Context context, final String repoOwner,
                                  final String repoName,
                                  final boolean fromPullRequest) {
    return new Intent(context, IssueMilestoneListActivity.class)
        .putExtra("owner", repoOwner)
        .putExtra("repo", repoName)
        .putExtra("from_pr", fromPullRequest);
  }

  private static final int REQUEST_CREATE_MILESTONE = 1000;

  private static final int[] TITLES =
      new int[] {R.string.open, R.string.closed};
  private static final int[][] HEADER_COLOR_ATTRS =
      new int[][] {{R.attr.colorIssueOpen, R.attr.colorIssueOpenDark},
                   {R.attr.colorIssueClosed, R.attr.colorIssueClosedDark}};

  private String mRepoOwner;
  private String mRepoName;
  private boolean mParentIsPullRequest;

  private FloatingActionButton mCreateFab;
  private IssueMilestoneListFragment mOpenFragment;

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Gh4Application.get().isAuthorized()) {
      CoordinatorLayout rootLayout = getRootLayout();
      mCreateFab = (FloatingActionButton)getLayoutInflater().inflate(
          R.layout.add_fab, rootLayout, false);
      mCreateFab.setOnClickListener(this);
      rootLayout.addView(mCreateFab);
    }
  }

  @Nullable
  @Override
  protected String getActionBarTitle() {
    return getString(R.string.issue_milestones);
  }

  @Nullable
  @Override
  protected String getActionBarSubtitle() {
    return mRepoOwner + "/" + mRepoName;
  }

  @Override
  protected int[] getTabTitleResIds() {
    return TITLES;
  }

  @Override
  protected Fragment makeFragment(final int position) {
    return IssueMilestoneListFragment.newInstance(
        mRepoOwner, mRepoName, position == 1, mParentIsPullRequest);
  }

  @Override
  protected void onFragmentInstantiated(final Fragment f, final int position) {
    if (position == 0) {
      mOpenFragment = (IssueMilestoneListFragment)f;
    }
  }

  @Override
  protected void onFragmentDestroyed(final Fragment f) {
    if (f == mOpenFragment) {
      mOpenFragment = null;
    }
  }

  @Override
  protected void onInitExtras(final Bundle extras) {
    super.onInitExtras(extras);
    mRepoOwner = extras.getString("owner");
    mRepoName = extras.getString("repo");
    mParentIsPullRequest = extras.getBoolean("from_pr", false);
  }

  @Override
  public void onRecyclerViewCreated(final Fragment fragment,
                                    final RecyclerView recyclerView) {
    if (fragment == mOpenFragment) {
      recyclerView.setTag(R.id.FloatingActionButtonScrollEnabled, new Object());
    }
  }

  @Override
  protected void onPageMoved(final int position, final float fraction) {
    super.onPageMoved(position, fraction);
    if (mCreateFab != null) {
      float openFraction = 1 - position - fraction;
      mCreateFab.setScaleX(openFraction);
      mCreateFab.setScaleY(openFraction);
      mCreateFab.setVisibility(openFraction == 0 ? View.INVISIBLE
                                                 : View.VISIBLE);
    }
  }

  @Override
  protected int[][] getTabHeaderColorAttrs() {
    return HEADER_COLOR_ATTRS;
  }

  @Override
  protected Intent navigateUp() {
    return IssueListActivity.makeIntent(this, mRepoOwner, mRepoName,
                                        mParentIsPullRequest);
  }

  @Override
  public void onClick(final View view) {
    startActivityForResult(
        IssueMilestoneEditActivity.makeCreateIntent(this, mRepoOwner, mRepoName,
                                                    mParentIsPullRequest),
        REQUEST_CREATE_MILESTONE);
  }

  @Override
  protected void invalidateFragments() {
    mOpenFragment = null;
    super.invalidateFragments();
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
                                  final Intent data) {
    if (requestCode == REQUEST_CREATE_MILESTONE) {
      if (resultCode == RESULT_OK) {
        onRefresh();
        setResult(RESULT_OK);
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
