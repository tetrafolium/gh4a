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
package com.gh4a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.gh4a.R;
import com.gh4a.ServiceFactory;
import com.gh4a.activities.CommitHistoryActivity;
import com.gh4a.adapter.FileAdapter;
import com.gh4a.adapter.RootAdapter;
import com.gh4a.utils.ApiHelpers;
import com.gh4a.utils.RxUtils;
import com.gh4a.utils.StringUtils;
import com.gh4a.widget.ContextMenuAwareRecyclerView;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Content;
import com.meisolsson.githubsdk.model.ContentType;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService;
import io.reactivex.Single;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ContentListFragment extends ListDataBaseFragment<Content>
    implements RootAdapter.OnItemClickListener<Content> {
  private static final int MENU_HISTORY = Menu.FIRST + 1;
  private static final int REQUEST_FILE_HISTORY = 1000;

  private static final Comparator<Content> COMPARATOR = (lhs, rhs) -> {
    boolean lhsIsDir = lhs.type() == ContentType.Directory;
    boolean rhsIsDir = rhs.type() == ContentType.Directory;
    if (lhsIsDir && !rhsIsDir) {
      // Directory before non-directory
      return -1;
    } else if (!lhsIsDir && rhsIsDir) {
      // Non-directory after directory
      return 1;
    } else {
      // Alphabetic order otherwise
      // return o1.compareTo(o2);
      return lhs.name().compareTo(rhs.name());
    }
  };

  private Repository mRepository;
  private String mPath;
  private String mRef;

  private ParentCallback mCallback;
  private FileAdapter mAdapter;

  public interface ParentCallback {
    void onContentsLoaded(ContentListFragment fragment, List<Content> contents);
    void onTreeSelected(Content content);
    void onCommitSelected(Commit commit);
    Set<String> getSubModuleNames(ContentListFragment fragment);
  }

  public static ContentListFragment
  newInstance(final Repository repository, final String path,
              final ArrayList<Content> contents, final String ref) {
    ContentListFragment f = new ContentListFragment();

    Bundle args = new Bundle();
    args.putString("path", path != null ? path : "");
    args.putString("ref", ref);
    args.putParcelable("repo", repository);
    args.putParcelableArrayList("contents", contents);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mRepository = getArguments().getParcelable("repo");
    mPath = getArguments().getString("path");
    mRef = getArguments().getString("ref");
    if (StringUtils.isBlank(mRef)) {
      mRef = mRepository.defaultBranch();
    }
  }

  @Override
  public void onAttach(final Context context) {
    super.onAttach(context);
    if (getParentFragment() instanceof ParentCallback) {
      mCallback = (ParentCallback)getParentFragment();
    } else if (context instanceof ParentCallback) {
      mCallback = (ParentCallback)context;
    } else {
      throw new ClassCastException("No callback provided");
    }
  }

  @Override
  protected RootAdapter<Content, ?> onCreateAdapter() {
    mAdapter = new FileAdapter(getActivity());
    mAdapter.setSubModuleNames(mCallback.getSubModuleNames(this));
    mAdapter.setContextMenuSupported(true);
    mAdapter.setOnItemClickListener(this);
    return mAdapter;
  }

  @Override
  protected void onRecyclerViewInflated(final RecyclerView view,
                                        final LayoutInflater inflater) {
    super.onRecyclerViewInflated(view, inflater);
    registerForContextMenu(view);
  }

  @Override
  protected int getEmptyTextResId() {
    return R.string.no_files_found;
  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View v,
                                  final ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    ContextMenuAwareRecyclerView.RecyclerContextMenuInfo info =
        (ContextMenuAwareRecyclerView.RecyclerContextMenuInfo)menuInfo;
    Content contents = mAdapter.getItemFromAdapterPosition(info.position);
    Set<String> subModules = mCallback.getSubModuleNames(this);

    if (subModules == null || !subModules.contains(contents.name())) {
      menu.add(Menu.NONE, MENU_HISTORY, Menu.NONE, R.string.history);
    }
  }

  @Override
  public boolean onContextItemSelected(final MenuItem item) {
    ContextMenuAwareRecyclerView.RecyclerContextMenuInfo info =
        (ContextMenuAwareRecyclerView.RecyclerContextMenuInfo)
            item.getMenuInfo();
    if (info.position >= mAdapter.getItemCount()) {
      return false;
    }

    int id = item.getItemId();
    if (id == MENU_HISTORY) {
      Content contents = mAdapter.getItemFromAdapterPosition(info.position);
      Intent intent = CommitHistoryActivity.makeIntent(
          getActivity(), mRepository.owner().login(), mRepository.name(), mRef,
          contents.path(), true);
      startActivityForResult(intent, REQUEST_FILE_HISTORY);
      return true;
    }

    return super.onContextItemSelected(item);
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode,
                               final Intent data) {
    if (requestCode == REQUEST_FILE_HISTORY &&
        resultCode == Activity.RESULT_OK) {
      Commit commit = data.getParcelableExtra("commit");
      mCallback.onCommitSelected(commit);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public String getPath() { return mPath; }

  public void onSubModuleNamesChanged(final Set<String> subModules) {
    if (mAdapter != null) {
      mAdapter.setSubModuleNames(subModules);
    }
  }

  @Override
  protected void onAddData(final RootAdapter<Content, ?> adapter,
                           final List<Content> data) {
    super.onAddData(adapter, data);
    mCallback.onContentsLoaded(this, data);
  }

  @Override
  public void onItemClick(final Content content) {
    mCallback.onTreeSelected(content);
  }

  @Override
  protected Single<List<Content>>
  onCreateDataSingle(final boolean bypassCache) {
    RepositoryContentService contentService =
        ServiceFactory.get(RepositoryContentService.class, bypassCache);
    String repoOwner = mRepository.owner().login();
    String repoName = mRepository.name();
    String ref = mRef != null ? mRef : mRepository.defaultBranch();

    return ApiHelpers.PageIterator
        .toSingle(page
                  -> contentService.getDirectoryContents(repoOwner, repoName,
                                                         mPath, ref, page))
        .compose(RxUtils.mapFailureToValue(HttpURLConnection.HTTP_NOT_FOUND,
                                           new ArrayList<Content>()))
        .compose(RxUtils.sortList(COMPARATOR));
  }

  @Override
  protected List<Content> onGetInitialData() {
    ArrayList<Content> contents =
        getArguments().getParcelableArrayList("contents");
    return contents != null && !contents.isEmpty() ? contents : null;
  }
}
