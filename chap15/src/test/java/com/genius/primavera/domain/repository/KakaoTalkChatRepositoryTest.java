package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.KakaoTalkChat;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.uwyn.jhighlight.fastutil.Hash;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
class KakaoTalkChatRepositoryTest {

	@Test
	public void kakaoRepository() throws IOException {
		KakaoTalkChatRepository<KakaoTalkChat> kakaoTalkChatRepository = name -> {
			ClassPathResource resource = new ClassPathResource(name);
			log.info("resource exists : {}", resource.exists());
			Reader reader = Files.newBufferedReader(resource.getFile().toPath());
			ColumnPositionMappingStrategy<KakaoTalkChat> strategy = new ColumnPositionMappingStrategy<>();
			strategy.setType(KakaoTalkChat.class);
			CsvToBean<KakaoTalkChat> csvToBean = new CsvToBeanBuilder(reader)
					.withMappingStrategy(strategy)
					.withSkipLines(1)
					.withType(KakaoTalkChat.class)
					.withQuoteChar('\0')
					.withSeparator(',')
					.withThrowExceptions(false)
					.build();
			return csvToBean.parse();
		};

		var kakaoTalkChats = kakaoTalkChatRepository.getKakaoTalkChatByName("kakaoTalk/chat/kakaoTalk_Chat.csv");
		Assertions.assertNotNull(kakaoTalkChats);
		log.info("kakaoTalkChats size : {}", kakaoTalkChats.size());

		// User 별 메세지
		Map<String, List<String>> countMessagesByUser = kakaoTalkChats.stream().collect(Collectors.groupingBy(KakaoTalkChat::getUser, Collectors.mapping(KakaoTalkChat::getMessage, Collectors.toList())));
		log.info("countMessagesByUser {}", countMessagesByUser);

		// User 별 <메세지, 날짜>
		Map<String, List<Tuple2<String, LocalDateTime>>> countMessageAndDateByUser = kakaoTalkChats.stream().collect(Collectors.groupingBy(KakaoTalkChat::getUser, Collectors.mapping(kakaoTalkChat -> new Tuple2<>(kakaoTalkChat.getMessage(), kakaoTalkChat.getDate()), Collectors.toList())));
		log.info("countMessageAndDateByUser {}", countMessageAndDateByUser);

		// User 메세지 갯수
		Map<String, Long> countMessageByUser = kakaoTalkChats.stream().collect(Collectors.groupingBy(KakaoTalkChat::getUser, Collectors.counting()));
		log.info("countMessageByUser {}", countMessageByUser);

		// User 메세지 갯수 정렬(DESC) 후 10개
		Map<String, Long> countMessageByUserOrder = kakaoTalkChats.stream()
				.collect(Collectors.groupingBy(KakaoTalkChat::getUser, Collectors.counting())).entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.limit(10)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		log.info("countMessageByUser {}", countMessageByUserOrder);

	}
}