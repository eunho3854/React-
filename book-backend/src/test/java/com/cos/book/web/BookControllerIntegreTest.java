package com.cos.book.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import com.cos.book.domain.Book;
import com.cos.book.domain.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// 통합 테스트 (모든 Bean들을 똑같이 IoC 올리고 테스트하는 것.)
// 통합 테스트는 실제 서버에 완벽하게 돌아감

/*
 *  WebEnvironment.MOCK // 실제 톰켓을 올리는 게 아니라, 다른 톰켓으로 테스트
 *  WebEnvironment.RANDOM_PORT // 실제 톰켓으로 테스트
 *  @AutoConfigureMockMvc MockMvc를 IoC에 등록해줌
 *  @Transactional은 각각의 테스트함수가 종료될 때마다 트랜잭션을 rollback 해주는 어노테이션
 */

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class BookControllerIntegreTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private BookRepository BookRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@BeforeEach
	public void init() {
		entityManager.createNativeQuery("ALTER TABLE book AUTO_INCREMENT = 1").executeUpdate();
	}
	
	// BDDMockito 패턴
		@Test
		public void save_테스트() throws Exception {
			
			// given (테스트하기 위한 준비)
			Book book = new Book(null, "스프링 따라하기", "코스");
			String content = new ObjectMapper().writeValueAsString(book); // 오브젝트를 json으로 바꿔줌
			
			// when (테스트 실행)
			ResultActions resultAction = mockMvc.perform(post("/book")
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.content(content)
					.accept(MediaType.APPLICATION_JSON_UTF8));
			
			// then (검증)
			resultAction
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("스프링 따라하기")) // $는 전체
				.andDo(MockMvcResultHandlers.print());
		}
		
		@Test
		public void findAll_테스트() throws Exception {

			// given
			java.util.List<Book> books = new ArrayList<>();
			books.add(new Book(null, "스프링부트 따라하기", "코스"));
			books.add(new Book(null, "리엑트 따라하기", "코스"));

			BookRepository.saveAll(books);
			
			// when
			ResultActions resultAction = mockMvc.perform(get("/book").accept(MediaType.APPLICATION_JSON_UTF8));

			// then
			resultAction.andExpect(status().isOk())
			.andExpect(jsonPath("$", Matchers.hasSize(2)))
			.andExpect(jsonPath("$.[0].title").value("스프링부트 따라하기"))
			.andDo(MockMvcResultHandlers.print());
		}
		
		@Test
		public void findById_테스트() throws Exception {

			// given
			Long id = 2L;
			java.util.List<Book> books = new ArrayList<>();
			books.add(new Book(null, "스프링부트 따라하기", "코스"));
			books.add(new Book(null, "리엑트 따라하기", "코스"));
			BookRepository.saveAll(books);

			// when
			ResultActions resultAction = mockMvc.perform(get("/book/{id}", id)
					.accept(MediaType.APPLICATION_JSON_UTF8));

			// then
			resultAction.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("리엑트 따라하기"))
			.andDo(MockMvcResultHandlers.print());
		}
		
		@Test
		public void update_테스트() throws Exception {

			// given
			Long id = 1L;
			
			java.util.List<Book> books = new ArrayList<>();
			books.add(new Book(null, "스프링부트 따라하기", "코스"));
			books.add(new Book(null, "리엑트 따라하기", "코스"));
			BookRepository.saveAll(books);
			
			Book book = new Book(null, "C++ 따라하기", "코스");
			String content = new ObjectMapper().writeValueAsString(book); // 오브젝트를 json으로 바꿔줌

			// when
			ResultActions resultAction = mockMvc.perform(put("/book/{id}", id)
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.content(content)
					.accept(MediaType.APPLICATION_JSON_UTF8));

			// then
			resultAction.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.title").value("C++ 따라하기"))
			.andDo(MockMvcResultHandlers.print());
		}
		
		@Test
		public void delete_테스트() throws Exception {

			// given
			BookRepository.saveAll(Arrays.asList(new Book(null, "스프링부트 따라하기", "코스"), new Book(null, "리엑트 따라하기", "코스")));
			Long id = 1L;
			
			// when
			ResultActions resultAction = mockMvc.perform(delete("/book/{id}", id));
			// then
			resultAction.andExpect(status().isOk())
			.andDo(MockMvcResultHandlers.print());
			
			MvcResult requsetResult = resultAction.andReturn();
			String result = requsetResult.getResponse().getContentAsString();
			// ok 받을 수 있음
			
			assertEquals("ok", result);
		}
	
}
