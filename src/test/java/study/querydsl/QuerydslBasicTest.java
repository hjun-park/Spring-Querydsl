package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@PersistenceContext
	EntityManager em;

	JPAQueryFactory queryFactory = new JPAQueryFactory(em);

	@BeforeEach
	public void before() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);

		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);

		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

	}

	@Test
	public void startQuerydsl() {
		// >> 09. QMember.member를 static으로 줄여서 member로 줄여쓰면 더욱 편함
		// selectFrom 으로 줄여 써보기
		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	// >> 10. 검색 조건 쿼리
	/*
		member.username.eq("member1") // username = 'member1'
		member.username.ne("member1") //username != 'member1'
		member.username.eq("member1").not() // username != 'member1'
		member.username.isNotNull() //이름이 is not null
		member.age.in(10, 20) // age in (10,20)
		member.age.notIn(10, 20) // age not in (10, 20)
		member.age.between(10,30) //between 10, 30
		member.age.goe(30) // age >= 30	 ( greater or equal )
		member.age.gt(30) // age > 30
		member.age.loe(30) // age <= 30 ( low or equal )
		member.age.lt(30) // age < 30
		member.username.like("member%") //like 검색
		member.username.contains("member") // like ‘%member%’ 검색
		member.username.startsWith("member") //like ‘member%’ 검색
	 */
	@Test
	public void search() {
//		Member findMember = queryFactory
//			.selectFrom(member)
//			.where(member.username.eq("member1")
//				.and(member.age.eq(10)))
//			.fetchOne();

		// and인 경우 where에 쉼표로 여러개 구분하면 좋다.
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.username.eq("member1"),
				member.age.eq(10)
			)
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");

	}


	// >> 11. 결과 조회 쿼리
	/*
		fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
		fetchOne() : 단 건 조회
		fetchFirst() : limit(1).fetchOne()
		fetchResults() : 페이징 정보 + total count Query 날아감
		fetchCount() : total count query
	 */
	@Test
	public void resultFetch() {
		// 결과를 리스트 조회
		List<Member> fetch = queryFactory
			.selectFrom(member)
			.fetch();

		Member fetchOne = queryFactory
			.selectFrom(QMember.member)
			.fetchOne();

		Member fetchFirst = queryFactory
			.selectFrom(QMember.member)
			.fetchFirst();

		// 페이징
		QueryResults<Member> results = queryFactory
			.selectFrom(member)
			.fetchResults();

		// 위 결과를 아래처럼 가져옴
		results.getTotal();
		List<Member> content = results.getResults();

		// totalCount 수 조회
		long total = queryFactory
			.selectFrom(member)
			.fetchCount();
	}


	// >> 12. 정렬
	// 1. 회원 나이 내림차순
	// 2. 회원 이름 올림차순
	// 단, 2에서 회원 이름 없으면 마지막에 출력 (nulls last)
	@Test
	public void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(), member.username.asc().nullsLast()) // nullsFirst도 있음
			.fetch();

		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);
		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}

	// >> 13. 페이징
	@Test
	public void paging() {
		queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())

	}


}
