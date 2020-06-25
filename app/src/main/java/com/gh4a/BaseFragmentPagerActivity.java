package com.gh4a;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.gh4a.fragment.LoadingFragmentBase;
import com.gh4a.widget.SwipeRefreshLayout;

public abstract class BaseFragmentPagerActivity extends BasePagerActivity
    implements SwipeRefreshLayout.ChildScrollDelegate {
  private FragmentAdapter mAdapter;

  @Override
  protected PagerAdapter createAdapter(final ViewGroup root) {
    mAdapter = new FragmentAdapter();
    return mAdapter;
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setChildScrollDelegate(this);
  }

  protected void invalidateFragments() { invalidatePages(); }

  @Override
  public void onBackPressed() {
    Fragment item = mAdapter.getCurrentFragment();
    if (item instanceof LoadingFragmentBase &&
        ((LoadingFragmentBase)item).onBackPressed()) {
      return;
    }
    super.onBackPressed();
  }

  @Override
  public void onRefresh() {
    for (int i = 0; i < mAdapter.getCount(); i++) {
      Fragment f = mAdapter.getExistingFragment(i);
      if (f instanceof RefreshableChild) {
        ((RefreshableChild)f).onRefresh();
      }
    }
    super.onRefresh();
  }

  @Override
  public boolean canChildScrollUp() {
    Fragment item = mAdapter.getCurrentFragment();
    if (item instanceof SwipeRefreshLayout.ChildScrollDelegate) {
      return ((SwipeRefreshLayout.ChildScrollDelegate)item).canChildScrollUp();
    }
    return false;
  }

  protected abstract int[] getTabTitleResIds();
  protected abstract Fragment makeFragment(int position);
  protected boolean fragmentNeedsRefresh(final Fragment object) {
    return false;
  }

  protected void onFragmentInstantiated(final Fragment f, final int position) {}

  protected void onFragmentDestroyed(final Fragment f) {}

  private class FragmentAdapter extends FragmentStatePagerAdapter {
    private final SparseArray<Fragment> mFragments = new SparseArray<>();
    private Fragment mCurrentFragment;

    public FragmentAdapter() { super(getSupportFragmentManager()); }

    @Override
    public int getCount() {
      int[] titleResIds = getTabTitleResIds();
      return titleResIds != null ? titleResIds.length : 0;
    }

    @Override
    public Fragment getItem(final int position) {
      return makeFragment(position);
    }

    @Override
    public Object instantiateItem(final ViewGroup container,
                                  final int position) {
      Fragment f = (Fragment)super.instantiateItem(container, position);
      mFragments.put(position, f);
      onFragmentInstantiated(f, position);
      return f;
    }

    private Fragment getExistingFragment(final int position) {
      return mFragments.get(position);
    }

    private Fragment getCurrentFragment() { return mCurrentFragment; }

    @Override
    public CharSequence getPageTitle(final int position) {
      return getString(getTabTitleResIds()[position]);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position,
                            final Object object) {
      super.destroyItem(container, position, object);
      mFragments.remove(position);
      onFragmentDestroyed((Fragment)object);
      if (object == mCurrentFragment) {
        mCurrentFragment = null;
      }
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position,
                               final Object object) {
      mCurrentFragment = (Fragment)object;
      super.setPrimaryItem(container, position, object);
    }

    @Override
    public int getItemPosition(final Object object) {
      if (object instanceof Fragment &&
          fragmentNeedsRefresh((Fragment)object)) {
        return POSITION_NONE;
      }
      return POSITION_UNCHANGED;
    }
  }
}
