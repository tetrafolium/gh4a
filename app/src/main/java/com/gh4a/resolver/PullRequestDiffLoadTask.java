package com.gh4a.resolver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;
import com.gh4a.ServiceFactory;
import com.gh4a.activities.PullRequestActivity;
import com.gh4a.activities.PullRequestDiffViewerActivity;
import com.gh4a.utils.ApiHelpers;
import com.gh4a.utils.RxUtils;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.ReviewComment;
import com.meisolsson.githubsdk.service.pull_request.PullRequestReviewCommentService;
import com.meisolsson.githubsdk.service.pull_request.PullRequestService;
import io.reactivex.Single;
import java.util.List;

public class PullRequestDiffLoadTask extends DiffLoadTask<ReviewComment> {
  @VisibleForTesting protected final int mPullRequestNumber;

  public PullRequestDiffLoadTask(final FragmentActivity activity,
                                 final String repoOwner, final String repoName,
                                 final DiffHighlightId diffId,
                                 final int pullRequestNumber) {
    super(activity, repoOwner, repoName, diffId);
    mPullRequestNumber = pullRequestNumber;
  }

  @Override
  protected @NonNull Intent getLaunchIntent(final String sha,
                                            final @NonNull GitHubFile file,
                                            final List<ReviewComment> comments,
                                            final DiffHighlightId diffId) {
    return PullRequestDiffViewerActivity.makeIntent(
        mActivity, mRepoOwner, mRepoName, mPullRequestNumber, sha,
        file.filename(), file.patch(), comments, -1, diffId.startLine,
        diffId.endLine, diffId.right, null);
  }

  @NonNull
  @Override
  protected Intent getFallbackIntent(final String sha) {
    return PullRequestActivity.makeIntent(mActivity, mRepoOwner, mRepoName,
                                          mPullRequestNumber);
  }

  @Override
  protected Single<String> getSha() {
    PullRequestService service =
        ServiceFactory.get(PullRequestService.class, false);
    return service.getPullRequest(mRepoOwner, mRepoName, mPullRequestNumber)
        .map(ApiHelpers::throwOnFailure)
        .map(pr -> pr.head().sha());
  }

  @Override
  protected Single<List<GitHubFile>> getFiles() {
    final PullRequestService service =
        ServiceFactory.get(PullRequestService.class, false);
    return ApiHelpers.PageIterator.toSingle(
        page
        -> service.getPullRequestFiles(mRepoOwner, mRepoName,
                                       mPullRequestNumber, page));
  }

  @Override
  protected Single<List<ReviewComment>> getComments() {
    final PullRequestReviewCommentService service =
        ServiceFactory.get(PullRequestReviewCommentService.class, false);
    return ApiHelpers.PageIterator
        .toSingle(page
                  -> service.getPullRequestComments(mRepoOwner, mRepoName,
                                                    mPullRequestNumber, page))
        .compose(RxUtils.filter(c -> c.position() != null));
  }
}
