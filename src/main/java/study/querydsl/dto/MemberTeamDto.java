package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

// >> 45. 빌더를 사용한 동적쿼리 작성
@Data
public class MemberTeamDto {

	private Long memberId;
	private String username;
	private int age;
	private Long teamId;
	private String teamName;

	@QueryProjection	// QType으로 생성
	public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
		this.memberId = memberId;
		this.username = username;
		this.age = age;
		this.teamId = teamId;
		this.teamName = teamName;
	}
}
