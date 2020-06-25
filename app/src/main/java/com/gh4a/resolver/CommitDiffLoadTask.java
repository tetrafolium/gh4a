package com.gh4a.resolver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;
import com.gh4a.ApiRequestException;
import com.gh4a.ServiceFactory;
import com.gh4a.activities.CommitActivity;
import com.gh4a.activities.CommitDiffViewerActivity;
import com.gh4a.utils.ApiHelpers;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import io.reactivex.Single;
import java.util.List;

public class CommitDiffLoadTask extends DiffLoadTask<GitComment> {
  @VisibleForTesting protected final String mSha;

  public CommitDiffLoadTask(final FragmentActivity activity,
                            final String repoOwner, final String repoName,
                            final DiffHighlightId diffId, final String sha) {
    super(activity, repoOwner, repoName, diffId);
    mSha = sha;
  }

  @Override
  protected @NonNull Intent getLaunchIntent(final String sha,
                                            final @NonNull GitHubFile file,
                                            final List<GitComment> comments,
                                            final DiffHighlightId diffId) {
    return CommitDiffViewerActivity.makeIntent(
        mActivity, mRepoOwner, mRepoName, sha, file.filename(), file.patch(),
        comments, diffId.startLine, diffId.endLine, diffId.right, null);
  }

  @Override
  protected @NonNull Intent getFallbackIntent(final String sha) {
    return CommitActivity.makeIntent(mActivity, mRepoOwner, mRepoName, sha);
  }

  @Override
  public Single<String> getSha() {
    return Single.just(mSha);
  }

  @Override
  protected Single<List<GitHubFile>> getFiles() throws ApiRequestException {
    RepositoryCommitService service =
        ServiceFactory.get(RepositoryCommitService.class, false);
    return service.getCommit(mRepoOwner, mRepoName, mSha)
        .map(ApiHelpers::throwOnFailure)
        .map(Commit::files);
  }

  @Override
  protected Single<List<GitComment>> getComments() throws ApiRequestException {
    final RepositoryCommentService service =
        ServiceFactory.get(RepositoryCommentService.class, false);
    return ApiHelpers.PageIterator.toSingle(
        page -> service.getCommitComments(mRepoOwner, mRepoName, mSha, page));
  }
}
