package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@PersistenceContext
	EntityManager em;

	// JPAQueryFactory를 필드로 제공
	JPAQueryFactory queryFactory;

	@BeforeEach
	public void before() {
		// EntityManager 의 경우 동시에 접근해도 영속성 컨텍스트를 트랜잭션마다 제공
		// 따라서 동시성 문제는 해결된다.
		queryFactory = new JPAQueryFactory(em);

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
		List<Member> result = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1)
			.limit(2)    // 하나씩 끊고 최대 2개까지
			.fetch();

		assertThat(result.size()).isEqualTo(2);

	}

	@Test
	public void paging2() {
		QueryResults<Member> memberQueryResults = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1)
			.limit(2)    // 하나씩 끊고 최대 2개까지
			.fetchResults();// 전체 결과 조회

		assertThat(memberQueryResults.getTotal()).isEqualTo(4);
		assertThat(memberQueryResults.getLimit()).isEqualTo(2);
		assertThat(memberQueryResults.getOffset()).isEqualTo(1);
		assertThat(memberQueryResults.getResults().size()).isEqualTo(2);    // 컨텐츠 사이즈

	}

	// >> 14. 집합
	@Test
	public void aggregation() {
		List<Tuple> result = queryFactory
			.select(
				member.count(),
				member.age.sum(),
				member.age.avg(),
				member.age.max(),
				member.age.min()
			)
			.from(member)
			.fetch();        // tuple 형식으로 꺼냄

		// tuple을 쓰는 이유 count, sum, avg, max 등등 데이터타입이 일정하지 않음
		// 이 대신에 DTO를 사용해도 된다.(권장)


		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);

	}

	// >> 15. 집합 :: groupby
	// 팀의 이름과 각 팀의 평균 연령 계산
	@Test
	public void group() throws Exception {
		List<Tuple> result = queryFactory
			.select(team.name, member.age.avg())    // 자료형이 다를 때에는 selectFrom 사용불가
			.from(member)
			.join(member.team, team)
			.groupBy(team.name)
//			.having(QTeam.team.name.eq("teamA"))	// 이런 방식으로 having절 넣을 수 있음
			.fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);    // (10 + 20) / 2

		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);    // (30 + 40) / 2

	}

	// >> 16. 조인
	// 팀 A에 소속된 모든 회원
	@Test
	public void join() {
		List<Member> result = queryFactory
			.selectFrom(member)
			.join(member.team, team)    // outerjoin인 leftjoin, rightjoin도 가능
			.where(team.name.eq("teamA"))
			.fetch();

		assertThat(result)
			.extracting("username")
			.containsExactly("member1", "member2");
	}

	// >> 17. 세타 조인 :: 전혀 연관성 없는 것 끼리도 조인이 가능하다.
	// 회원의 이름이 팀 이름과 같은 회원 조회
	@Test
	public void theta_join() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));

		List<Member> result = queryFactory
			.select(member)
			.from(member, team)    // 테이블 2개를 부름 ( 카티션 프로덕트 )
			.where(member.username.eq(team.name)) // 모든 것들을 가져와서 다 조인 후 where절로 거름
			.fetch();

		assertThat(result)
			.extracting("username")
			.containsExactly("teamA", "teamB");
	}

	// >> 18. ON 절을 활용한 조인 ( 조인 대상 필터링 )
	// 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	// JPQL: select m, t from Member m left join m.team t on t.name = "teamA"
	@Test
	public void join_on_filtering() {
		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(member.team, team).on(team.name.eq("teamA"))
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	// >> 19. ON 절을 활용한 조인 ( 연관관계 없는 엔티티 outer join )
	@Test
	public void join_on_no_relation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));

		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(team).on(member.username.eq(team.name))    // member.team이 아닌 그냥 team(막 조인할거기 때문에)
			// member.team, team을 하게되면 member의 teamID 같은 걸 찾지만 넣지 않으면 그냥 다 찾는다.
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@PersistenceUnit
	EntityManagerFactory emf;

	// >> 20. 페치 조인 ( 없을 때 )
	@Test
	public void fetchJoinNo() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		// 현재 LAZY이기 때문에 member는 조회되고 Team은 조회되지 않는다.

		// emf 를 이용하면 현재 엔티티가 load 되었는지 확인 가능
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isFalse();

	}


	// >> 21. 페치 조인 ( 적용 )
	@Test
	public void fetchJoinUse() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
			.selectFrom(member)
			.join(member.team, team).fetchJoin()    // 페치 조인 사용
			.where(member.username.eq("member1"))
			.fetchOne();

		// emf 를 이용하면 현재 엔티티가 load 되었는지 확인 가능
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isTrue();

	}


	// >> 22. where 서브 쿼리
	// 01. 나이가 가장 많은 회원 조회
	@Test
	public void subQuery() {

		// 바깥에 있는 member 와 alias가 겹치지 않기 위해서 QMember를 만들어 줌
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(    // where절 결과는 40
				JPAExpressions    // 서브쿼리
					.select(memberSub.age.max())
					.from(memberSub)
			))
			.fetch();

		assertThat(result).extracting("age")
			.containsExactly(40);
	}

	// >> 23. where 서브 쿼리
	// 02. 나이가 평균 이상인 회원 조회
	@Test
	public void subQueryGoe() {

		// 바깥에 있는 member 와 alias가 겹치지 않기 위해서 QMember를 만들어 줌
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.goe(
				JPAExpressions    // 서브쿼리
					.select(memberSub.age.avg())
					.from(memberSub)
			))
			.fetch();

		assertThat(result).extracting("age")
			.containsExactly(30, 40);
	}

	// >> 24. where 서브 쿼리
	// 03. 서브 쿼리 IN
	@Test
	public void subQueryIn() {

		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.in(
				JPAExpressions    // 서브쿼리
					.select(memberSub.age)
					.from(memberSub)
					.where(memberSub.age.gt(10))
			))
			.fetch();

		assertThat(result).extracting("age")
			.containsExactly(20, 30, 40);
	}

	// >> 24. select 서브 쿼리
	// 멤버들의 전체 평균 나이 출력
	@Test
	public void selectSubQuery() {

		QMember memberSub = new QMember("memberSub");

		List<Tuple> result = queryFactory
			.select(member.username,
				JPAExpressions	// static import도 가능함
					.select(memberSub.age.avg())
					.from(memberSub))
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}

	}

	/*
		서브쿼리는 from 절 (인라인 뷰) 제공하지 않는다. JPQL도 마찬가지

		from절 서브쿼리 해결방안
		1. 서브쿼리를 join으로 변경한다. (일반적으로 효율적이고 좋은 방식)
		2. application에서 쿼리를 2번 분리해서 실행한다.
		3. nativeSQL을 사용한다.

		쿼리에서 어떻게든 풀려고 하지말고, ( query에서 날짜를 맞추려고 하는 등 )
		DB는 데이터 필터링, 그룹핑 수행, (데이터를 최소화해서 가져오도록 수행)
		Logic은 application이나 presentation(화면)에서 해결
	 */


	// >> 24. case문
	@Test
	public void basicCase() {
		List<String> result = queryFactory
			.select(member.age
				.when(10).then("열살")
				.when(20).then("스무살")
				.otherwise("기타"))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	// >> 25. 복잡한 case문
	// 사실 case문은 꼭 필요한 경우가 아니라면 application 단에서 해결하는 것이 좋다.
	@Test
	public void complexCase() {
		List<String> result = queryFactory
			.select(new CaseBuilder()
				.when(member.age.between(0, 20)).then("0~20살")
				.when(member.age.between(21, 30)).then("21~30살")
				.otherwise("기타"))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}


	// >> 26. 상수
	// case와 마찬가지로 필요한 경우에만 사용
	@Test
	public void constant() {
		List<Tuple> result = queryFactory
			.select(member.username, Expressions.constant("A"))	// 멤버이름, A 출력
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@Test
	public void concat() {
		// username_age
		// age는 문자가 아니기 때문에 cast 필요 ( 이건 쓸 일이 많다 )
		List<String> result = queryFactory
			.select(member.username.concat("_").concat(member.age.stringValue()))
			.from(member)
			.where(member.username.eq("member1"))
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

}
