package com.gh4a.model;

import io.reactivex.Single;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TrendService {
  @GET("trends/trending_{type}-all.json")
  Single<Response<List<Trend>>> getTrends(@Path("type") String type);
}
