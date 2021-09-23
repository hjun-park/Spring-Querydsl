package study.querydsl.entity;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

// >> 02. 빌드 시에 build.gradle 에서 셋팅한 위치인 build/generated/querydsl 에서 QHello가 생성되는 걸 확인
// 방법은 ./gradle clean 후 ./gradle compileQuery
// build에 셋팅해놓았기 때문에 github에 안 올라가게 셋팅됨
@Entity
@Getter @Setter
public class Hello {

	@Id @GeneratedValue
	private Long id;
}
