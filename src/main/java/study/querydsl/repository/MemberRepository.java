package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;

// >> 54. 순수 JPA에서 스프링 데이터 JPA 리포지토리로 변경
// 즉, 스프링 데이터 JPA인 JpaRepository
// 그리고 사용자 정의 리포지토리 (querydsl 사용) MemberRepositoryCustom
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
	// 메소드 이름을 갖고 자동으로 JPQL 쿼리 만듦
	List<Member> findByUsername(String username);

}
