package com.my.test;

import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.my.test.AsyncRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AsyncRestClient.
 */
public class AsyncRestClientTest {

	private HttpClient client;

	@BeforeEach
	void setUp() {
		// Initialize HttpClient instance before each test method
		client = HttpClient.newHttpClient();
	}

	@Test
	void testFetchJsonUsers() throws InterruptedException, ExecutionException {
		String usersUrl = "https://jsonplaceholder.typicode.com/users";

		CompletableFuture<JsonNode> usersFuture = AsyncRestClient.fetchJson(client, usersUrl, "GET", null);

		// Wait for the asynchronous operation to complete
		JsonNode users = usersFuture.get();

		// Assert that the users data is not null
		assertNotNull(users);
		// Ensure the response is an array type JsonNode
		assertTrue(users.isArray());
	}

	@Test
	void testFetchJsonPosts() throws InterruptedException, ExecutionException {
		String postsUrl = "https://jsonplaceholder.typicode.com/posts";

		CompletableFuture<JsonNode> postsFuture = AsyncRestClient.fetchJson(client, postsUrl, "GET", null);

		// Wait for the asynchronous operation to complete
		JsonNode posts = postsFuture.get();

		// Assert that the posts data is not null
		assertNotNull(posts);
		// Ensure the response is an array type JsonNode
		assertTrue(posts.isArray());
	}

	@Test
	void testProcessUsersAndPosts() throws InterruptedException, ExecutionException {
		String usersUrl = "https://jsonplaceholder.typicode.com/users";
		String postsUrl = "https://jsonplaceholder.typicode.com/posts";

		CompletableFuture<JsonNode> usersFuture = AsyncRestClient.fetchJson(client, usersUrl, "GET", null);
		CompletableFuture<JsonNode> postsFuture = AsyncRestClient.fetchJson(client, postsUrl, "GET", null);

		// Mock the process to capture printed output
		CompletableFuture<Void> processFuture = CompletableFuture.runAsync(() -> {
			AsyncRestClient.processUsersAndPosts(usersFuture, postsFuture);
		});

		// Wait for the processing to complete
		processFuture.get();

	}
}
