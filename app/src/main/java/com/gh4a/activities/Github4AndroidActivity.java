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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import com.gh4a.BaseActivity;
import com.gh4a.BuildConfig;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.ServiceFactory;
import com.gh4a.fragment.UserPasswordLoginDialogFragment;
import com.gh4a.utils.ApiHelpers;
import com.gh4a.utils.IntentUtils;
import com.gh4a.utils.RxUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.request.RequestToken;
import com.meisolsson.githubsdk.service.OAuthService;
import com.meisolsson.githubsdk.service.users.UserService;
import io.reactivex.Single;

/**
 * The Github4Android activity.
 */
public class Github4AndroidActivity extends BaseActivity
    implements View.OnClickListener,
               UserPasswordLoginDialogFragment.ParentCallback {
  private static final String OAUTH_URL =
      "https://github.com/login/oauth/authorize";
  private static final String PARAM_CLIENT_ID = "client_id";
  private static final String PARAM_CODE = "code";
  private static final String PARAM_SCOPE = "scope";
  private static final String PARAM_CALLBACK_URI = "redirect_uri";

  private static final Uri CALLBACK_URI = Uri.parse("gh4a://oauth");
  public static final String SCOPES = "user,repo,gist";

  private static final int REQUEST_SETTINGS = 10000;

  private View mContent;
  private View mProgress;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Gh4Application app = Gh4Application.get();
    if (app.isAuthorized()) {
      if (!handleIntent(getIntent())) {
        goToToplevelActivity();
      }
      finish();
    } else {
      setContentView(R.layout.main);

      AppBarLayout abl = findViewById(R.id.header);
      abl.setEnabled(false);

      FrameLayout contentContainer =
          (FrameLayout)findViewById(R.id.content).getParent();
      contentContainer.setForeground(null);

      findViewById(R.id.oauth_login_button).setOnClickListener(this);
      findViewById(R.id.user_pw_login_button).setOnClickListener(this);
      mContent = findViewById(R.id.welcome_container);
      mProgress = findViewById(R.id.login_progress_container);

      handleIntent(getIntent());
    }
  }

  @Override
  protected void onNewIntent(final Intent intent) {
    if (!handleIntent(intent)) {
      super.onNewIntent(intent);
    }
  }

  private boolean handleIntent(final Intent intent) {
    Uri data = intent.getData();
    if (data != null && data.getScheme().equals(CALLBACK_URI.getScheme()) &&
        data.getHost().equals(CALLBACK_URI.getHost())) {
      OAuthService service = ServiceGenerator.createAuthService();
      RequestToken request = RequestToken.builder()
                                 .clientId(BuildConfig.CLIENT_ID)
                                 .clientSecret(BuildConfig.CLIENT_SECRET)
                                 .code(data.getQueryParameter(PARAM_CODE))
                                 .build();

      service.getToken(request)
          .map(ApiHelpers::throwOnFailure)
          .flatMap(token -> {
            UserService userService = ServiceFactory.get(
                UserService.class, true, null, token.accessToken(), null);
            Single<User> userSingle =
                userService.getUser().map(ApiHelpers::throwOnFailure);
            return Single.zip(Single.just(token), userSingle,
                              (t, user) -> Pair.create(t.accessToken(), user));
          })
          .compose(RxUtils::doInBackground)
          .subscribe(pair
                     -> onLoginFinished(pair.first, pair.second),
                     this::handleLoadFailure);
      return true;
    }

    return false;
  }

  @Override
  protected int getLeftNavigationDrawerMenuResource() {
    return R.menu.home_nav_drawer;
  }

  @IdRes
  protected int getInitialLeftDrawerSelection(final Menu menu) {
    menu.setGroupCheckable(R.id.navigation, false, false);
    menu.setGroupCheckable(R.id.explore, false, false);
    menu.setGroupVisible(R.id.my_items, false);
    return super.getInitialLeftDrawerSelection(menu);
  }

  @Override
  public boolean onNavigationItemSelected(final @NonNull MenuItem item) {
    super.onNavigationItemSelected(item);
    switch (item.getItemId()) {
    case R.id.settings:
      startActivityForResult(new Intent(this, SettingsActivity.class),
                             REQUEST_SETTINGS);
      return true;
    case R.id.search:
      startActivity(SearchActivity.makeIntent(this));
      return true;
    case R.id.bookmarks:
      startActivity(new Intent(this, BookmarkListActivity.class));
      return true;
    case R.id.pub_timeline:
      startActivity(new Intent(this, TimelineActivity.class));
      return true;
    case R.id.blog:
      startActivity(new Intent(this, BlogListActivity.class));
      return true;
    case R.id.trend:
      startActivity(new Intent(this, TrendingActivity.class));
      return true;
    }
    return false;
  }

  @Override
  protected boolean canSwipeToRefresh() {
    return false;
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
                                  final Intent data) {
    if (requestCode == REQUEST_SETTINGS) {
      boolean themeChange =
          data != null &&
          data.getBooleanExtra(SettingsActivity.RESULT_EXTRA_THEME_CHANGED,
                               false);
      if (themeChange) {
        Intent intent = new Intent(getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void onClick(final View view) {
    if (view.getId() == R.id.oauth_login_button) {
      launchOauthLogin(this);
      mContent.setVisibility(View.GONE);
      mProgress.setVisibility(View.VISIBLE);
    } else if (view.getId() == R.id.user_pw_login_button) {
      UserPasswordLoginDialogFragment.newInstance(SCOPES).show(
          getSupportFragmentManager(), "foo");
    }
  }

  @Override
  public void onLoginFinished(final String token, final User user) {
    Gh4Application.get().addAccount(user, token);
    goToToplevelActivity();
    finish();
  }

  @Override
  public void onLoginFailed(final Throwable error) {
    handleLoadFailure(error);
  }

  public static void launchOauthLogin(final Activity activity) {
    Uri uri =
        Uri.parse(OAUTH_URL)
            .buildUpon()
            .appendQueryParameter(PARAM_CLIENT_ID, BuildConfig.CLIENT_ID)
            .appendQueryParameter(PARAM_SCOPE, SCOPES)
            .appendQueryParameter(PARAM_CALLBACK_URI, CALLBACK_URI.toString())
            .build();
    IntentUtils.openInCustomTabOrBrowser(activity, uri);
  }
}
