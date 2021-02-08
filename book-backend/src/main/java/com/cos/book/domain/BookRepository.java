package com.cos.book.domain;

import org.springframework.data.jpa.repository.JpaRepository;

// @Repository 적어야 스프링 IoC에 빈으로 등록이 되는데.. !!
// JpaRepository를 extends하면 생략가능함.
// JpaRepository안에 함수들 사용가능

public interface BookRepository extends JpaRepository<Book, Long> {

}
