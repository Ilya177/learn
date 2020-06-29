package com.epam.learn;

import java.util.List;
import java.util.Map;
import lombok.Getter;

public class  Response {

	@Getter
	private final List<String> messages;

	@Getter
	private final Map<String, Object> input;

	public Response(List<String> messages, Map<String, Object> input) {
		this.messages = messages;
		this.input = input;
	}
}
