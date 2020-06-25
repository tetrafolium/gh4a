package com.gh4a.model;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "feed", strict = false)
public class GitHubFeed {
  @ElementList(name = "entry", inline = true) public List<Feed> feed;
}
