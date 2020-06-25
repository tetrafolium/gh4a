package com.gh4a.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gh4a.BaseFragmentPagerActivity;
import com.gh4a.R;
import com.gh4a.ServiceFactory;
import com.gh4a.db.BookmarksProvider;
import com.gh4a.fragment.CommitListFragment;
import com.gh4a.fragment.ContentListContainerFragment;
import com.gh4a.fragment.RepositoryEventListFragment;
import com.gh4a.fragment.RepositoryFragment;
import com.gh4a.utils.ApiHelpers;
import com.gh4a.utils.IntentUtils;
import com.gh4a.utils.RxUtils;
import com.gh4a.utils.UiUtils;
import com.meisolsson.githubsdk.model.Branch;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryBranchService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class RepositoryActivity extends BaseFragmentPagerActivity implements
	CommitListFragment.ContextSelectionCallback,
	                                ContentListContainerFragment.CommitSelectionCallback {
public static Intent makeIntent(final Context context, final Repository repo) {
	return makeIntent(context, repo, null);
}

public static Intent makeIntent(final Context context, final Repository repo, final String ref) {
	return makeIntent(context, repo.owner().login(), repo.name(), ref);
}

public static Intent makeIntent(final Context context, final String repoOwner, final String repoName) {
	return makeIntent(context, repoOwner, repoName, null);
}

public static Intent makeIntent(final Context context, final String repoOwner, final String repoName, final String ref) {
	return makeIntent(context, repoOwner, repoName, ref, null, PAGE_REPO_OVERVIEW);
}

public static Intent makeIntent(final Context context, final String repoOwner, final String repoName, final String ref,
                                final String initialPath, final int initialPage) {
	if (TextUtils.isEmpty(ref)) {
		ref = null;
	}
	return new Intent(context, RepositoryActivity.class)
	       .putExtra("owner", repoOwner)
	       .putExtra("repo", repoName)
	       .putExtra("ref", ref)
	       .putExtra("initial_path", initialPath)
	       .putExtra("initial_page", initialPage);
}

private static final String STATE_KEY_SELECTED_REF = "selected_ref";

private static final int ID_LOADER_REPO = 0;

public static final int PAGE_REPO_OVERVIEW = 0;
public static final int PAGE_FILES = 1;
public static final int PAGE_COMMITS = 2;
public static final int PAGE_ACTIVITY = 3;

private static final int[] TITLES = new int[] {
	R.string.about, R.string.repo_files, R.string.commits, R.string.repo_activity
};

private String mRepoOwner;
private String mRepoName;
private ActionBar mActionBar;
private int mInitialPage;
private String mInitialPath;

private Repository mRepository;
private List<Branch> mBranches;
private List<Branch> mTags;
private String mSelectedRef;

private RepositoryFragment mRepositoryFragment;
private ContentListContainerFragment mContentListFragment;
private CommitListFragment mCommitListFragment;
private RepositoryEventListFragment mActivityFragment;

@Override
public void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	if (savedInstanceState != null) {
		mSelectedRef = savedInstanceState.getString(STATE_KEY_SELECTED_REF);
	}

	mActionBar = getSupportActionBar();
	mActionBar.setTitle(mRepoOwner + "/" + mRepoName);
	mActionBar.setDisplayHomeAsUpEnabled(true);

	setContentShown(false);

	loadRepository(false);
}

@Override
protected void onInitExtras(final Bundle extras) {
	super.onInitExtras(extras);
	mRepoOwner = extras.getString("owner");
	mRepoName = extras.getString("repo");
	mSelectedRef = extras.getString("ref");
	mInitialPage = extras.getInt("initial_page", -1);
	mInitialPath = extras.getString("initial_path");
}

@Override
protected void onSaveInstanceState(final Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putString(STATE_KEY_SELECTED_REF, mSelectedRef);
}

@Override
public void onCommitSelectedAsBase(final Commit commit) {
	setSelectedRef(commit.sha());
}

@Override
public boolean baseSelectionAllowed() {
	return true;
}

private void updateTitle() {
	mActionBar.setSubtitle(getCurrentRef());
	invalidateFragments();
}

private String getCurrentRef() {
	if (!TextUtils.isEmpty(mSelectedRef)) {
		return mSelectedRef;
	}
	return mRepository.defaultBranch();
}

private String getBookmarkUrl() {
	String url = "https://github.com/" + mRepoOwner + "/" + mRepoName;
	String ref = getCurrentRef();
	return ref.equals(mRepository.defaultBranch()) ? url : url + "/tree/" + ref;
}

@Override
protected int[] getTabTitleResIds() {
	return mRepository != null ? TITLES : null;
}

@Override
protected Fragment makeFragment(final int position) {
	switch (position) {
	case 0:
		return RepositoryFragment.newInstance(mRepository, mSelectedRef);
	case 1:
		Fragment f = ContentListContainerFragment.newInstance(mRepository,
		                                                      mSelectedRef, mInitialPath);
		mInitialPath = null;
		return f;
	case 2:
		return CommitListFragment.newInstance(mRepository, mSelectedRef);
	case 3:
		return RepositoryEventListFragment.newInstance(mRepository);
	}
	return null;
}

@Override
protected void onFragmentInstantiated(final Fragment f, final int position) {
	switch (position) {
	case 0:
		mRepositoryFragment = (RepositoryFragment) f;
		break;
	case 1:
		mContentListFragment = (ContentListContainerFragment) f;
		break;
	case 2:
		mCommitListFragment = (CommitListFragment) f;
		break;
	case 3:
		mActivityFragment = (RepositoryEventListFragment) f;
		break;
	}
}

@Override
protected void onFragmentDestroyed(final Fragment f) {
	if (f == mRepositoryFragment) {
		mRepositoryFragment = null;
	} else if (f == mContentListFragment) {
		mContentListFragment = null;
	} else if (f == mCommitListFragment) {
		mCommitListFragment = null;
	} else if (f == mActivityFragment) {
		mActivityFragment = null;
	}
}

@Override
protected boolean fragmentNeedsRefresh(final Fragment fragment) {
	if (fragment instanceof CommitListFragment && mCommitListFragment == null) {
		return true;
	} else if (fragment instanceof ContentListContainerFragment
	           && mContentListFragment == null) {
		return true;
	} else if (fragment instanceof RepositoryFragment && mRepositoryFragment == null) {
		return true;
	} else if (fragment instanceof RepositoryEventListFragment && mActivityFragment == null) {
		return true;
	}
	return false;
}

@Override
public void onRefresh() {
	mRepositoryFragment = null;
	mContentListFragment = null;
	mActivityFragment = null;
	mRepository = null;
	mBranches = null;
	mTags = null;
	clearRefDependentFragments();
	setContentShown(false);
	invalidateTabs();
	loadRepository(true);
	super.onRefresh();
}

@Override
public void onBackPressed() {
	if (mContentListFragment != null) {
		if (getPager().getCurrentItem() == 1 && mContentListFragment.handleBackPress()) {
			return;
		}
	}
	super.onBackPressed();
}

@Override
public boolean onCreateOptionsMenu(final Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.repo_menu, menu);
	return super.onCreateOptionsMenu(menu);
}

@Override
public boolean onPrepareOptionsMenu(final Menu menu) {
	if (mRepository == null) {
		menu.removeItem(R.id.ref);
		menu.removeItem(R.id.bookmark);
		menu.removeItem(R.id.zip_download);
	} else {
		MenuItem bookmarkAction = menu.findItem(R.id.bookmark);
		if (bookmarkAction != null) {
			bookmarkAction.setTitle(BookmarksProvider.hasBookmarked(this, getBookmarkUrl())
			                ? R.string.remove_bookmark
			                : R.string.bookmark);
		}
	}

	return super.onPrepareOptionsMenu(menu);
}

@Override
protected Intent navigateUp() {
	return UserActivity.makeIntent(this, mRepoOwner);
}

@Override
public boolean onOptionsItemSelected(final MenuItem item) {
	Uri url = IntentUtils.createBaseUriForRepo(mRepoOwner, mRepoName).build();
	switch (item.getItemId()) {
	case R.id.ref:
		loadOrShowRefSelection();
		return true;
	case R.id.share:
		IntentUtils.share(this, mRepoOwner + "/" + mRepoName, url);
		return true;
	case R.id.browser:
		IntentUtils.launchBrowser(this, url);
		return true;
	case R.id.search:
		String initialSearch = "repo:" + mRepoOwner + "/" + mRepoName + " ";
		startActivity(SearchActivity.makeIntent(this, initialSearch,
		                                        SearchActivity.SEARCH_TYPE_CODE, false));
		return true;
	case R.id.bookmark:
		String bookmarkUrl = getBookmarkUrl();
		if (BookmarksProvider.hasBookmarked(this, bookmarkUrl)) {
			BookmarksProvider.removeBookmark(this, bookmarkUrl);
		} else {
			BookmarksProvider.saveBookmark(this, mActionBar.getTitle().toString(),
			                               BookmarksProvider.Columns.TYPE_REPO, bookmarkUrl, getCurrentRef(), true);
		}
		return true;
	case R.id.zip_download:
		String zipUrl = url + "/archive/" + getCurrentRef() + ".zip";
		UiUtils.enqueueDownloadWithPermissionCheck(this, zipUrl, "application/zip",
		                                           mRepoName + "-" + getCurrentRef() + ".zip", null, null);
		return true;
	}
	return super.onOptionsItemSelected(item);
}

private void showRefSelectionDialog() {
	final BranchAndTagAdapter adapter = new BranchAndTagAdapter();
	int current = -1, master = -1, count = adapter.getCount();

	for (int i = 0; i < count; i++) {
		Branch item = adapter.getItem(i);
		if (item.name().equals(mSelectedRef) || item.commit().sha().equals(mSelectedRef)) {
			current = i;
		}
		if (item.name().equals(mRepository.defaultBranch())) {
			master = i;
		}
	}
	if (mSelectedRef == null && current == -1) {
		current = master;
	}

	new AlertDialog.Builder(this)
	.setCancelable(true)
	.setTitle(R.string.repo_select_ref_dialog_title)
	.setSingleChoiceItems(adapter, current, (dialog, which)->{
			setSelectedRef(adapter.getItem(which).name());
			dialog.dismiss();
		})
	.setNegativeButton(R.string.cancel, null)
	.show();
}

private void setSelectedRef(final String selectedRef) {
	mSelectedRef = selectedRef;
	clearRefDependentFragments();
	updateTitle();
}

private void clearRefDependentFragments() {
	if (mRepositoryFragment != null) {
		mRepositoryFragment.setRef(mSelectedRef);
	}
	if (mContentListFragment != null) {
		mContentListFragment.setRef(mSelectedRef);
	}
	mCommitListFragment = null;
}

private void loadRepository(final boolean force) {
	RepositoryService service = ServiceFactory.get(RepositoryService.class, force);
	service.getRepository(mRepoOwner, mRepoName)
	.map(ApiHelpers::throwOnFailure)
	.compose(makeLoaderSingle(ID_LOADER_REPO, force))
	.subscribe(result->{
			mRepository = result;
			updateTitle();
			invalidateTabs();
			// Apply initial page selection first time the repo is loaded
			if (mInitialPage >= PAGE_REPO_OVERVIEW && mInitialPage <= PAGE_ACTIVITY) {
			        getPager().setCurrentItem(mInitialPage);
			        mInitialPage = -1;
			}
			setContentShown(true);
			supportInvalidateOptionsMenu();
		}, this::handleLoadFailure);
}

private void loadOrShowRefSelection() {
	if (mBranches != null) {
		showRefSelectionDialog();
	} else {
		final RepositoryBranchService branchService =
			ServiceFactory.get(RepositoryBranchService.class, false);
		final RepositoryService repoService = ServiceFactory.get(RepositoryService.class, false);

		Single<List<Branch> > branchSingle = ApiHelpers.PageIterator
		                                     .toSingle(page->branchService.getBranches(mRepoOwner, mRepoName, page));
		Single<List<Branch> > tagSingle = ApiHelpers.PageIterator
		                                  .toSingle(page->repoService.getTags(mRepoOwner, mRepoName, page));

		registerTemporarySubscription(Single.zip(branchSingle, tagSingle, Pair::create)
		                              .compose(RxUtils::doInBackground)
		                              .compose(RxUtils.wrapWithProgressDialog(this, R.string.loading_msg))
		                              .subscribe(result->{
				mBranches = result.first;
				mTags = result.second;
				showRefSelectionDialog();
			}, this::handleLoadFailure));
	}
}

private class BranchAndTagAdapter extends BaseAdapter {
private final ArrayList<Branch> mItems;
private final LayoutInflater mInflater;
private final int mBranchDrawableResId;
private final int mTagDrawableResId;
private final int mFirstTagIndex;

public BranchAndTagAdapter() {
	mItems = new ArrayList<>();
	mItems.addAll(mBranches);
	mItems.addAll(mTags);
	mFirstTagIndex = mBranches.size();
	mInflater = LayoutInflater.from(RepositoryActivity.this);
	mBranchDrawableResId = UiUtils.resolveDrawable(RepositoryActivity.this, R.attr.branchIcon);
	mTagDrawableResId = UiUtils.resolveDrawable(RepositoryActivity.this, R.attr.tagIcon);
}

@Override
public int getCount() {
	return mItems.size();
}

@Override
public Branch getItem(final int position) {
	return mItems.get(position);
}

@Override
public long getItemId(final int position) {
	return position;
}

@Override
public View getView(final int position, final View convertView, final ViewGroup parent) {
	if (convertView == null) {
		convertView = mInflater.inflate(R.layout.row_branch, parent, false);
	}
	ImageView icon = convertView.findViewById(R.id.icon);
	TextView title = convertView.findViewById(R.id.title);

	icon.setImageResource(position >= mFirstTagIndex
	                          ? mTagDrawableResId : mBranchDrawableResId);
	title.setText(mItems.get(position).name());

	return convertView;
}
}
}
