package hello;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Todo {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	private String description;

	private boolean complete;

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public boolean isComplete() {
		return complete;
	}

	public Todo() {
	}

	public Todo(String description, boolean complete) {
		this.description = description;
		this.complete = complete;
	}

}