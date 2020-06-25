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

import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gh4a.R;
import com.gh4a.activities.BlogActivity;
import com.gh4a.adapter.CommonFeedAdapter;
import com.gh4a.adapter.RootAdapter;
import com.gh4a.model.Feed;
import com.gh4a.utils.IntentUtils;
import com.gh4a.utils.SingleFactory;

import io.reactivex.Single;

public class BlogListFragment extends ListDataBaseFragment<Feed> implements
    RootAdapter.OnItemClickListener<Feed> {
    public static BlogListFragment newInstance() {
        return new BlogListFragment();
    }

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.blog_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.browser:
            IntentUtils.launchBrowser(getActivity(), Uri.parse("https://blog.github.com"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected RootAdapter<Feed, ? extends RecyclerView.ViewHolder> onCreateAdapter() {
        CommonFeedAdapter adapter = new CommonFeedAdapter(getActivity(), true);
        adapter.setOnItemClickListener(this);
        return adapter;
    }

    @Override
    protected int getEmptyTextResId() {
        return R.string.no_blogs_found;
    }

    @Override
    public void onItemClick(final Feed blog) {
        startActivity(BlogActivity.makeIntent(getActivity(), blog));
    }

    @Override
    protected Single<List<Feed>> onCreateDataSingle(final boolean bypassCache) {
        return SingleFactory.loadBlogFeed();
    }
}
