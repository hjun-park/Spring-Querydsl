package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

// >> 42. 순수 JPA 리포지토리와 querydsl 사용
@Repository
public class MemberJpaRepository {

	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	public MemberJpaRepository(EntityManager em) {
		this.em = em;	// 인젝션
		this.queryFactory = new JPAQueryFactory(em);	// entity manager 이용
	}

	public void save(Member member) {
		em.persist(member);
	}

	public Optional<Member> findById(Long id) {
		Member findMember = em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}

	// >> 44. findAll을 querydsl로 변경
	public List<Member> findAll_Querydsl() {
		return queryFactory
			.selectFrom(member)
			.fetch();
	}

	public List<Member> findByUsername(String username) {
		return em.createQuery("select m from Member m where m.username = :username", Member.class)
			.setParameter("username", username)
			.getResultList();
	}

	// >> 44. findByUsername을 querydsl로 변경
	// JPQL과 다르게 빌드하는 과정에서 컴파일 오류로 쿼리오류를 잡기 때문에 편함
	public List<Member> findByUsername_Querdsl(String username) {
		return queryFactory
			.selectFrom(member)
			.where(member.username.eq(username))
			.fetch();
	}

	// >> 47. builder 사용 동적 쿼리
	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

		BooleanBuilder builder = new BooleanBuilder();
		// username으로 null이 올 수 있지만 "" 빈 문자열이 오는 경우도 있음
		// 이럴 경우 StringUTils 사용
		// 01. builder 를 이용해서 동적 쿼리를 만듦과 동시에
		if (StringUtils.hasText(condition.getUsername())) {
			builder.and(member.username.eq(condition.getUsername()));
		}
		if (StringUtils.hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}
		if (condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}
		if (condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}

		// 02. 최적화
		return queryFactory
			.select(new QMemberTeamDto(
				member.id.as("memberId"),	// db에서는 id라 되어 있으니까
				member.username,
				member.age,
				team.id.as("teamId"),
				team.name.as("teamName")))
			.from(member)
			.leftJoin(member.team, team)
			.fetch();
	}

	// >> 49. where 절을 이용한 동적 쿼리와 파라미터 최적화
	// => 47 과정에서 BooleanBuilder 사용한 것 보다 더 보기 좋음
	public List<MemberTeamDto> searchByWhere(MemberSearchCondition condition) {
		return queryFactory
			.select(new QMemberTeamDto(
				member.id.as("memberId"),    // db에서는 id라 되어 있으니까
				member.username,
				member.age,
				team.id.as("teamId"),
				team.name.as("teamName")))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
				ageGoe(condition.getAgeGoe()),
				ageLoe(condition.getAgeLoe())
			)
			.fetch();
	}

	// predicate 보다는 BooleanExpression으로 할 것
	private BooleanExpression usernameEq(String username) {
		return StringUtils.hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;

	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}


	// >> 50. 만약 49처럼 DTO가 아닌 Entity로 바로 반환을 해야 한다면 ?
	// 그냥 DTO를 member로 나오게 바꾸면 된다.
	// 간단히 수정하고 쓸 수 있다는 그 재사용성이 제일 좋다.
	public List<Member> searchByWhereAsEntity(MemberSearchCondition condition) {
		return queryFactory
			.selectFrom(member)
			.leftJoin(member.team, team)
			.where(
				usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
//				ageGoe(condition.getAgeGoe()),
//				ageLoe(condition.getAgeLoe())
				// >> 51. 이런 식으로 조립할 수 있다. null 체크 주의
				ageBetween(condition.getAgeLoe(), condition.getAgeGoe())

			)
			.fetch();
	}

	private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
		return ageGoe(ageLoe).and(ageGoe(ageGoe));
	}


}
