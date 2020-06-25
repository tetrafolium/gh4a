package com.gh4a.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;

import com.gh4a.ServiceFactory;
import com.gh4a.utils.ApiHelpers;
import com.meisolsson.githubsdk.model.GitHubCommentBase;
import com.meisolsson.githubsdk.model.request.CommentRequest;
import com.meisolsson.githubsdk.service.issues.IssueCommentService;

import io.reactivex.Single;

public class EditIssueCommentActivity extends EditCommentActivity {
    public static Intent makeIntent(final Context context, final String repoOwner,
                                    final String repoName, final int issueNumber, final long id, final String body,
                                    final @AttrRes int highlightColorAttr) {
        Intent intent = new Intent(context, EditIssueCommentActivity.class)
        .putExtra("issue", issueNumber);
        return EditCommentActivity.fillInIntent(intent,
                                                repoOwner, repoName, id, 0L, body, highlightColorAttr);
    }

    @Override
    protected Single<GitHubCommentBase> createComment(final String repoOwner, final String repoName,
            final String body, final long replyToCommentId) {
        int issueNumber = getIntent().getIntExtra("issue", 0);
        IssueCommentService service = ServiceFactory.get(IssueCommentService.class, false);
        CommentRequest request = CommentRequest.builder().body(body).build();
        return service.createIssueComment(repoOwner, repoName, issueNumber, request)
               .map(ApiHelpers::throwOnFailure);
    }

    @Override
    protected Single<GitHubCommentBase> editComment(final String repoOwner, final String repoName,
            final long commentId, final String body) {
        IssueCommentService service = ServiceFactory.get(IssueCommentService.class, false);
        CommentRequest request = CommentRequest.builder().body(body).build();
        return service.editIssueComment(repoOwner, repoName, commentId, request)
               .map(ApiHelpers::throwOnFailure);
    }
}
