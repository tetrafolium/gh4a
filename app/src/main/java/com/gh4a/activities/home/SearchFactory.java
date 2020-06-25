package com.gh4a.activities.home;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import com.gh4a.R;
import com.gh4a.fragment.SearchFragment;

public class SearchFactory extends FragmentFactory {
  private static final int[] TITLES = new int[] {R.string.search};

  public SearchFactory(final HomeActivity activity) { super(activity); }

  @Override
  protected @StringRes int getTitleResId() {
    return R.string.search;
  }

  @Override
  protected int[] getTabTitleResIds() {
    return TITLES;
  }

  @Override
  protected Fragment makeFragment(final int position) {
    return SearchFragment.newInstance(SearchFragment.SEARCH_TYPE_REPO, null,
                                      false);
  }
}
