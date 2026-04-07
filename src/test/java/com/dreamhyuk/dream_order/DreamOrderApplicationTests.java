package com.dreamhyuk.dream_order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@SpringBootTest
class DreamOrderApplicationTests {

	@MockBean
	private ElasticsearchOperations elasticsearchOperations;

	@Test
	void contextLoads() {
	}

}
