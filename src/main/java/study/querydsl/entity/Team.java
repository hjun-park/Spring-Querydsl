package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

	@Id
	@GeneratedValue
	@Column(name = "team_id")
	private Long id;
	private String name;

	@OneToMany(mappedBy = "team")
	private List<Member> members = new ArrayList<>();

	// >> 06. NoArgsConstructor의 PROTECT는 아래와 같은 생성자를 만들어준다.
	// 그래서 JPA의 기본 생성자 만드는 요구사항을 충족할 수 있다.
	//	protected Team() {
	//
	//	}


	public Team(String name) {
		this.name = name;
	}


}
