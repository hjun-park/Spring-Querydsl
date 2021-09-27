package study.querydsl.dto;

import lombok.Data;

// >> 46. 회원명, 팀명, 나이(ageGoe, ageLoe)
@Data
public class MemberSearchCondition {

	private String username;
	private String teamName;
	private Integer ageGoe;
	private Integer ageLoe;
}
