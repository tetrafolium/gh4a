package com.gh4a.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import com.gh4a.adapter.RootAdapter;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import java.util.List;

public abstract class ListDataBaseFragment<T> extends LoadingListFragmentBase {
  private RootAdapter<T, ? extends RecyclerView.ViewHolder> mAdapter;
  private Disposable mSubscription;

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setContentShown(false);
    loadData(false);
  }

  @Override
  public void onRefresh() {
    setContentShown(false);
    if (mSubscription != null) {
      mSubscription.dispose();
    }
    loadData(true);
    if (mAdapter != null) {
      mAdapter.clear();
    }
  }

  @Override
  protected boolean hasDividers() {
    return !mAdapter.isCardStyle();
  }

  protected void onAddData(final RootAdapter<T, ?> adapter,
                           final List<T> data) {
    adapter.addAll(data);
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onRecyclerViewInflated(final RecyclerView view,
                                        final LayoutInflater inflater) {
    super.onRecyclerViewInflated(view, inflater);
    mAdapter = onCreateAdapter();
    view.setAdapter(mAdapter);
    updateEmptyState();
  }

  @Override
  protected boolean hasCards() {
    return mAdapter.isCardStyle();
  }

  private void loadData(final boolean force) {
    List<T> initialData = force ? null : onGetInitialData();
    if (initialData != null) {
      handleNewData(initialData);
    } else {
      mSubscription =
          onCreateDataSingle(force)
              .compose(makeLoaderSingle(0, force))
              .subscribe(this::handleNewData, this::handleLoadFailure);
    }
  }

  private void handleNewData(final List<T> result) {
    mAdapter.clear();
    onAddData(mAdapter, result);
    setContentShown(true);
    updateEmptyState();
  }

  protected abstract Single<List<T>> onCreateDataSingle(boolean bypassCache);
  protected List<T> onGetInitialData() { return null; }
  protected abstract RootAdapter<T, ? extends RecyclerView.ViewHolder>
  onCreateAdapter();
}
