package com.gh4a.activities.home;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import com.gh4a.R;
import com.gh4a.fragment.BookmarkListFragment;
import com.gh4a.fragment.StarredRepositoryListFragment;

public class BookmarkFactory extends FragmentFactory {
  private static final int[] TAB_TITLES =
      new int[] {R.string.bookmarks, R.string.starred};

  private final String mUserLogin;

  public BookmarkFactory(final HomeActivity activity, final String userLogin) {
    super(activity);
    mUserLogin = userLogin;
  }

  @Override
  @StringRes
  protected int getTitleResId() {
    return R.string.bookmarks_and_stars;
  }

  @Override
  protected int[] getTabTitleResIds() {
    return TAB_TITLES;
  }

  @Override
  protected Fragment makeFragment(final int position) {
    if (position == 1) {
      return StarredRepositoryListFragment.newInstance(mUserLogin);
    }
    return BookmarkListFragment.newInstance();
  }
}
