package com.gh4a.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import com.gh4a.ServiceFactory;
import com.gh4a.utils.ApiHelpers;
import com.meisolsson.githubsdk.model.GitHubCommentBase;
import com.meisolsson.githubsdk.model.request.CommentRequest;
import com.meisolsson.githubsdk.model.request.pull_request.CreateReviewComment;
import com.meisolsson.githubsdk.service.pull_request.PullRequestReviewCommentService;
import io.reactivex.Single;

public class EditPullRequestCommentActivity extends EditCommentActivity {
  public static Intent makeIntent(final Context context, final String repoOwner,
                                  final String repoName, final int prNumber,
                                  final long id, final long replyToCommentId,
                                  final String body,
                                  final @AttrRes int highlightColorAttr) {
    // This activity only supports editing or replying to comments,
    // not creating new unrelated ones.
    if (id == 0L && replyToCommentId == 0L) {
      throw new IllegalStateException("Only editing and replying allowed");
    }
    Intent intent = new Intent(context, EditPullRequestCommentActivity.class)
                        .putExtra("pr", prNumber);
    return EditCommentActivity.fillInIntent(intent, repoOwner, repoName, id,
                                            replyToCommentId, body,
                                            highlightColorAttr);
  }

  @Override
  protected Single<GitHubCommentBase>
  createComment(final String repoOwner, final String repoName,
                final String body, final long replyToCommentId) {
    int prNumber = getIntent().getIntExtra("pr", 0);
    PullRequestReviewCommentService service =
        ServiceFactory.get(PullRequestReviewCommentService.class, false);
    CreateReviewComment request = CreateReviewComment.builder()
                                      .body(body)
                                      .inReplyTo(replyToCommentId)
                                      .build();
    return service.createReviewComment(repoOwner, repoName, prNumber, request)
        .map(ApiHelpers::throwOnFailure);
  }

  @Override
  protected Single<GitHubCommentBase>
  editComment(final String repoOwner, final String repoName,
              final long commentId, final String body) {
    PullRequestReviewCommentService service =
        ServiceFactory.get(PullRequestReviewCommentService.class, false);
    CommentRequest request = CommentRequest.builder().body(body).build();
    return service.editReviewComment(repoOwner, repoName, commentId, request)
        .map(ApiHelpers::throwOnFailure);
  }
}
