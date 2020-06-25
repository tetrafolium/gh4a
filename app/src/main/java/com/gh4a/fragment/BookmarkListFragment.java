package com.gh4a.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import com.gh4a.BaseActivity;
import com.gh4a.R;
import com.gh4a.adapter.BookmarkAdapter;
import com.gh4a.db.BookmarksProvider;
import com.gh4a.resolver.BrowseFilter;

public class BookmarkListFragment extends LoadingListFragmentBase
    implements LoaderManager.LoaderCallbacks<Cursor>,
               BookmarkAdapter.OnItemInteractListener {

  private ItemTouchHelper mItemTouchHelper;

  public static BookmarkListFragment newInstance() {
    return new BookmarkListFragment();
  }

  private BookmarkAdapter mAdapter;

  @Override
  public void onActivityCreated(final @Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setContentShown(false);
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  protected void onRecyclerViewInflated(final RecyclerView view,
                                        final LayoutInflater inflater) {
    super.onRecyclerViewInflated(view, inflater);
    mAdapter = new BookmarkAdapter(getActivity(), this);
    view.setAdapter(mAdapter);

    BookmarkDragHelperCallback callback =
        new BookmarkDragHelperCallback(getBaseActivity(), mAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(view);

    updateEmptyState();
  }

  @Override
  public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
    return new CursorLoader(getActivity(),
                            BookmarksProvider.Columns.CONTENT_URI, null, null,
                            null, BookmarksProvider.Columns.ORDER_ID + " ASC");
  }

  @Override
  public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
    mAdapter.swapCursor(data);
    setContentShown(true);
    updateEmptyState();
  }

  @Override
  public void onStop() {
    if (mAdapter != null) {
      mAdapter.updateOrder(getActivity());
    }
    super.onStop();
  }

  @Override
  public void onLoaderReset(final Loader<Cursor> loader) {
    mAdapter.swapCursor(null);
    updateEmptyState();
  }

  @Override
  public void onRefresh() {
    setContentShown(false);
    Loader loader = getLoaderManager().getLoader(0);
    if (loader != null) {
      loader.onContentChanged();
    }
  }

  @Override
  protected int getEmptyTextResId() {
    return R.string.no_bookmarks;
  }

  @Override
  public void onItemClick(final long id, final String url) {
    startActivity(BrowseFilter.makeRedirectionIntent(getActivity(),
                                                     Uri.parse(url), null));
  }

  @Override
  public void onItemDrag(final RecyclerView.ViewHolder viewHolder) {
    mItemTouchHelper.startDrag(viewHolder);
  }

  public static class BookmarkDragHelperCallback
      extends ItemTouchHelper.SimpleCallback {
    private final BaseActivity mBaseActivity;
    private final BookmarkAdapter mAdapter;

    public BookmarkDragHelperCallback(final BaseActivity baseActivity,
                                      final BookmarkAdapter adapter) {
      super(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
      mBaseActivity = baseActivity;
      mAdapter = adapter;
    }

    @Override
    public boolean onMove(final RecyclerView recyclerView,
                          final RecyclerView.ViewHolder viewHolder,
                          final RecyclerView.ViewHolder target) {
      int fromPos = viewHolder.getAdapterPosition();
      int toPos = target.getAdapterPosition();

      mAdapter.onItemMoved(fromPos, toPos);
      return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder,
                         final int direction) {
      mAdapter.onItemSwiped(viewHolder);
    }

    @Override
    public void onSelectedChanged(final RecyclerView.ViewHolder viewHolder,
                                  final int actionState) {
      super.onSelectedChanged(viewHolder, actionState);

      boolean isDragging = actionState == ItemTouchHelper.ACTION_STATE_DRAG;
      mBaseActivity.setCanSwipeToRefresh(!isDragging);
    }

    @Override
    public boolean isLongPressDragEnabled() {
      return false;
    }
  }
}
