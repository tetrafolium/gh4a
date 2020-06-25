package com.gh4a.resolver;

public class DiffHighlightId {
  public final String fileHash;
  public final int startLine;
  public final int endLine;
  public final boolean right;

  public DiffHighlightId(final String fileHash, final int startLine,
                         final int endLine, final boolean right) {
    this.fileHash = fileHash;
    this.startLine = startLine;
    this.endLine = endLine;
    this.right = right;
  }
}
