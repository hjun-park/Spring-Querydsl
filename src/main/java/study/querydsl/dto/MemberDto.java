package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)	// 기본 생성자
public class MemberDto {

	private String username;
	private int age;

	@QueryProjection // >> 34. DTO도 Qtype 생성 가능(CompileQuerydsl 수행)
	public MemberDto(String username, int age) {
		this.username = username;
		this.age = age;
	}


}
