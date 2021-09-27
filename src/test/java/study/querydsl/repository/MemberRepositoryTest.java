package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// >> 55. 스프링 데이터 JPA 리포지토리 테스트
@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;

	@Test
	public void basicTest() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		Member findMember = memberRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);

		List<Member> result1 = memberRepository.findAll();
		assertThat(result1).contains(member);

		List<Member> result2 = memberRepository.findByUsername("member1");
		assertThat(result2).contains(member);
	}

	// >> 48. 47에 대한 테스트
	@Test
	public void searchTest() {
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

		// ======================================================================
		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setAgeGoe(30);
		condition.setAgeLoe(40);
		condition.setTeamName("teamB");

		List<MemberTeamDto> result = memberRepository.search(condition);
//		List<MemberTeamDto> result = memberJpaRepository.searchByWhere(condition);
		// username 필드에 정확히 member3, member4가 존재해야 한다.
		assertThat(result).extracting("username").containsExactly("member3", "member4");

	}

	// >> 59. 페이징 테스트
	@Test
	public void searchPageSimpleTest() {
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

		// ======================================================================
		MemberSearchCondition condition = new MemberSearchCondition();
		PageRequest pageRequest = PageRequest.of(0, 3);// spring data는 0페이지부터 시작  3개를 가져올거야

		Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");

	}


}
