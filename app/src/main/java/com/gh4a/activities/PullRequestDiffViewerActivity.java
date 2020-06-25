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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.gh4a.ServiceFactory;
import com.gh4a.utils.ApiHelpers;
import com.gh4a.utils.IntentUtils;
import com.gh4a.utils.RxUtils;
import com.gh4a.widget.ReactionBar;
import com.meisolsson.githubsdk.model.PositionalCommentBase;
import com.meisolsson.githubsdk.model.Reaction;
import com.meisolsson.githubsdk.model.Reactions;
import com.meisolsson.githubsdk.model.ReviewComment;
import com.meisolsson.githubsdk.model.request.ReactionRequest;
import com.meisolsson.githubsdk.service.reactions.ReactionService;
import com.meisolsson.githubsdk.service.pull_request.PullRequestReviewCommentService;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Response;

public class PullRequestDiffViewerActivity extends DiffViewerActivity<ReviewComment> {
public static Intent makeIntent(final Context context, final String repoOwner, final String repoName, final int number,
                                final String commitSha, final String path, final String diff, final List<ReviewComment> comments,
                                final int initialLine, final int highlightStartLine, final int highlightEndLine, final boolean highlightIsRight,
                                final IntentUtils.InitialCommentMarker initialComment) {
	Intent intent = new Intent(context, PullRequestDiffViewerActivity.class)
	                .putExtra("number", number);
	return DiffViewerActivity.fillInIntent(intent, repoOwner, repoName, commitSha, path,
	                                       diff, comments, initialLine, highlightStartLine, highlightEndLine,
	                                       highlightIsRight, initialComment);
}

private int mPullRequestNumber;

@Override
protected void openCommentDialog(final long id, final long replyToId, final String line, final int position,
                                 final int leftLine, final int rightLine, final PositionalCommentBase commitComment) {
	String body = commitComment == null ? "" : commitComment.body();
	Intent intent = EditPullRequestDiffCommentActivity.makeIntent(this,
	                                                              mRepoOwner, mRepoName, mSha, mPath, line, leftLine, rightLine,
	                                                              position, id, body, mPullRequestNumber, replyToId);
	startActivityForResult(intent, REQUEST_EDIT);
}

@Override
protected void onInitExtras(final Bundle extras) {
	super.onInitExtras(extras);
	mPullRequestNumber = extras.getInt("number", -1);
}

@Override
protected Single<List<ReviewComment> > createCommentSingle(final boolean bypassCache) {
	final PullRequestReviewCommentService service =
		ServiceFactory.get(PullRequestReviewCommentService.class, bypassCache);
	return ApiHelpers.PageIterator
	       .toSingle(page->service.getPullRequestComments(
				 mRepoOwner, mRepoName, mPullRequestNumber, page))
	       .compose(RxUtils.filter(c->c.position() != null));
}

@Override
protected Uri createUrl(final String lineId, final long replyId) {
	Uri.Builder builder = IntentUtils.createBaseUriForRepo(mRepoOwner, mRepoName)
	                      .appendPath("pull")
	                      .appendPath(String.valueOf(mPullRequestNumber))
	                      .appendPath("files");
	if (replyId > 0L) {
		builder.fragment("r" + replyId);
	} else {
		builder.fragment("diff-" + ApiHelpers.md5(mPath) + lineId);
	}
	return builder.build();
}

@Override
protected Intent navigateUp() {
	return PullRequestActivity.makeIntent(this, mRepoOwner, mRepoName, mPullRequestNumber);
}

@Override
protected boolean canReply() {
	return true;
}

@Override
protected PositionalCommentBase onUpdateReactions(final PositionalCommentBase comment,
                                                  final Reactions reactions) {
	return ((ReviewComment) comment).toBuilder()
	       .reactions(reactions)
	       .build();
}

@Override
protected Single<Response<Void> > doDeleteComment(final long id) {
	PullRequestReviewCommentService service =
		ServiceFactory.get(PullRequestReviewCommentService.class, false);
	return service.deleteComment(mRepoOwner, mRepoName, id);
}

@Override
public Single<List<Reaction> > loadReactionDetails(final ReactionBar.Item item, final boolean bypassCache) {
	final CommitCommentWrapper comment = (CommitCommentWrapper) item;
	final ReactionService service = ServiceFactory.get(ReactionService.class, bypassCache);
	return ApiHelpers.PageIterator
	       .toSingle(page->service.getPullRequestReviewCommentReactions(
				 mRepoOwner, mRepoName, comment.comment.id(), page));
}

@Override
public Single<Reaction> addReaction(final ReactionBar.Item item, final String content) {
	CommitCommentWrapper comment = (CommitCommentWrapper) item;
	ReactionService service = ServiceFactory.get(ReactionService.class, false);
	ReactionRequest request = ReactionRequest.builder().content(content).build();

	return service.createPullRequestReviewCommentReaction(mRepoOwner, mRepoName, comment.comment.id(), request)
	       .map(ApiHelpers::throwOnFailure);
}
}
