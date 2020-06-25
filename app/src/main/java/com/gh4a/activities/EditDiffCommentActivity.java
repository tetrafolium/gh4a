package com.gh4a.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gh4a.R;
import com.gh4a.ServiceFactory;
import com.gh4a.utils.ApiHelpers;
import com.meisolsson.githubsdk.model.GitHubCommentBase;
import com.meisolsson.githubsdk.model.request.CommentRequest;
import com.meisolsson.githubsdk.model.request.repository.CreateCommitComment;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;

import io.reactivex.Single;

public class EditDiffCommentActivity extends EditCommentActivity {
public static Intent makeIntent(final Context context, final String repoOwner, final String repoName,
                                final String commitId, final String path, final String line, final int leftLine, final int rightLine, final int position,
                                final long id, final String body) {
	Intent intent = new Intent(context, EditDiffCommentActivity.class)
	                .putExtra("commit_id", commitId)
	                .putExtra("path", path)
	                .putExtra("line", line)
	                .putExtra("left_line", leftLine)
	                .putExtra("right_line", rightLine)
	                .putExtra("position", position);
	return EditCommentActivity.fillInIntent(intent,
	                                        repoOwner, repoName, id, 0L, body, R.attr.colorIssueOpen);
}

@Override
public void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	View header = getLayoutInflater().inflate(R.layout.edit_commit_comment_header, null);
	mEditorSheet.addHeaderView(header);

	TextView line = header.findViewById(R.id.line);
	Bundle extras = getIntent().getExtras();
	line.setText(extras.getString("line"));

	TextView title = header.findViewById(R.id.title);
	title.setText(getString(R.string.commit_comment_dialog_title, extras.getInt("left_line"),
	                        extras.getInt("right_line")));
}

@Override
protected Single<GitHubCommentBase> createComment(final String repoOwner, final String repoName,
                                                  final String body, final long replyToCommentId) {
	Bundle extras = getIntent().getExtras();
	String commitId = extras.getString("commit_id");
	RepositoryCommentService service = ServiceFactory.get(RepositoryCommentService.class, false);
	CreateCommitComment request = CreateCommitComment.builder()
	                              .body(body)
	                              .path(extras.getString("path"))
	                              .position(extras.getInt("position"))
	                              .build();
	return service.createCommitComment(repoOwner, repoName, commitId, request)
	       .map(ApiHelpers::throwOnFailure);
}

@Override
protected Single<GitHubCommentBase> editComment(final String repoOwner, final String repoName,
                                                final long commentId, final String body) {
	RepositoryCommentService service = ServiceFactory.get(RepositoryCommentService.class, false);
	CommentRequest request = CommentRequest.builder().body(body).build();
	return service.editCommitComment(repoOwner, repoName, commentId, request)
	       .map(ApiHelpers::throwOnFailure);
}
}
