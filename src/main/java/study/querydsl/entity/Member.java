package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;


// >> 07. ToString 대상에는 연관관계 매핑한 것이 없어야 한다.
// 만약 있으면 서로 들어가서 출력하기 때문에 무한루프 발생 가능
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {

	@Id
	@GeneratedValue
	@Column(name = "member_id")
	private Long id;
	private String username;
	private int age;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	// >> 08. this 생성자를 이용하여 입력 대상 이용
	public Member(String username) {
		this(username, 0);
	}

	public Member(String username, int age) {
		this(username, age, null);
	}

	public Member(String username, int age, Team team) {
		this.username = username;
		this.age = age;
		if (team != null) {
			changeTeam(team);
		}
	}

	public void changeTeam(Team team) {
		this.team = team;
		team.getMembers().add(this);	// 리스트에 자신을 추가
	}

}
