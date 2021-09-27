package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

// >> 56. 사용자 정의 리포지토리
public interface MemberRepositoryCustom {
	List<MemberTeamDto> search(MemberSearchCondition condition);

	// >> 58. querydsl 페이징 연동
	Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
	Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);


}
