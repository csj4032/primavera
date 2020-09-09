package com.genius.primavera.domain.repository;

import java.io.IOException;
import java.util.List;

public interface KakaoTalkChatRepository<T> {

	List<T> getKakaoTalkChatByName(String name) throws IOException;
}
