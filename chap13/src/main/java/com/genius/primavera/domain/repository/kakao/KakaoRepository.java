package com.genius.primavera.domain.repository.kakao;

import com.genius.primavera.domain.model.kakao.KakaoFriend;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface KakaoRepository {

	@GET("/v1/api/talk/friends")
	Call<List<KakaoFriend>> getKakaoFriends(@Query("limit") String limit);

}
