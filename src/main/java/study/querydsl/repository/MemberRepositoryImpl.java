package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

// >> 57. JPA에서 인터페이스를 구현하는 사용자 정의 리포지토리 이름 끝에는 Impl이 들어가야 함
@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public MemberRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	// >> 49. where 절을 이용한 동적 쿼리와 파라미터 최적화
	// => 47 과정에서 BooleanBuilder 사용한 것 보다 더 보기 좋음
	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
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

	// pageable
	@Override
	public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
		QueryResults<MemberTeamDto> results = queryFactory
			.select(new QMemberTeamDto(
				member.id.as("memberId"),
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
			.offset(pageable.getOffset())        // 몇 번째부터 시작
			.limit(pageable.getPageSize())        // 한 페이지에 몇개씩 ?
			.fetchResults();// fetchResults를 쓰면 querydsl이 content, count쿼리를 날려줌

		// 데이터를 꺼내는 과정  (쿼리가 2번 나감)
		List<MemberTeamDto> content = results.getResults();    // content 즉, 실제 데이터
		long total = results.getTotal();

		return new PageImpl<>(content, pageable, total);

	}

	// >> 60. complex의 경우 content (값) 그리고 total count 쿼리를 분리한다.
	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
		List<MemberTeamDto> content = queryFactory
			.select(new QMemberTeamDto(
				member.id.as("memberId"),
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
			.offset(pageable.getOffset())        // 몇 번째부터 시작
			.limit(pageable.getPageSize())        // 한 페이지에 몇개씩 ?
			.fetch(); // complex의 경우 content 그대로 뽑는다.

		// 내가 직접 total count query를 날림
		// content 쿼리는 복잡한데 total은 쉽게 조회할 수 있는 경우가 있다.
		// count쿼리를 날려보고 없다면 content 쿼리를 날리지 않는다는 등 최적화를 할 수 있다.
		// 근데 데이터가 별로 없다면 그냥 simple 방식으로 하는게 기운 빠지지 않는다.
		long total = queryFactory
			.select(member)
			.from(member)
			.leftJoin(member.team, team)
			.where(
				usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
				ageGoe(condition.getAgeGoe()),
				ageLoe(condition.getAgeLoe())
			)
			.fetchCount();

		return new PageImpl<>(content, pageable, total);
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


}
