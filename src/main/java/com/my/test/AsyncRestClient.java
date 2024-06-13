package com.my.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Asynchronous REST Client to fetch data from JSONPlaceholder APIs.
 */
public class AsyncRestClient {

	/**
	 * Main method to demonstrate fetching users and posts concurrently from JSONPlaceholder.
	 * Prints out usernames, addresses, and corresponding post titles.
	 *
	 * @param args Command-line arguments (not used).
	 */
	public static void main(String[] args) {
		String usersUrl = "https://jsonplaceholder.typicode.com/users";
		String postsUrl = "https://jsonplaceholder.typicode.com/posts";

		HttpClient client = HttpClient.newHttpClient();

		// Fetch users and posts concurrently
		CompletableFuture<JsonNode> usersFuture = fetchJson(client, usersUrl, "GET", null);
		CompletableFuture<JsonNode> postsFuture = fetchJson(client, postsUrl, "GET", null);

		// Process and combine results of users and posts
		processUsersAndPosts(usersFuture, postsFuture);
	}

	/**
	 * Fetches JSON data from the specified URL asynchronously using the provided HttpClient.
	 * Supports HTTP methods: GET, POST, PUT, DELETE.
	 *
	 * @param client HttpClient instance to use for sending requests.
	 * @param url URL of the endpoint to fetch JSON data from.
	 * @param method HTTP method to use (GET, POST, PUT, DELETE).
	 * @param body Optional request body for POST or PUT requests.
	 * @return CompletableFuture containing the parsed JsonNode response.
	 */
	 static CompletableFuture<JsonNode> fetchJson(HttpClient client, String url, String method, String body) {
		HttpRequest request;

		switch (method.toUpperCase()) {
		case "POST":
			request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""))
					.build();
			break;
		case "PUT":
			request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""))
					.build();
			break;
		case "DELETE":
			request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.DELETE()
					.build();
			break;
		case "GET":
		default:
			request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.GET()
					.build();
			break;
		}

		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(bodyResponse -> {
					try {
						ObjectMapper mapper = new ObjectMapper();
						return mapper.readTree(bodyResponse);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
	}


	/**
	 * Processes users and posts data fetched from the API, and prints user information
	 * along with corresponding post titles.
	 *
	 * @param usersFuture CompletableFuture containing users data.
	 * @param postsFuture CompletableFuture containing posts data.
	 */
	 static void processUsersAndPosts(CompletableFuture<JsonNode> usersFuture, CompletableFuture<JsonNode> postsFuture) {
		usersFuture.thenAcceptBoth(postsFuture, (users, posts) -> {
			users.forEach(user -> {
				String username = user.get("username").asText();
				JsonNode address = user.get("address");
				String street = address.get("street").asText();
				String suite = address.get("suite").asText();
				String city = address.get("city").asText();
				String zipcode = address.get("zipcode").asText();

				posts.forEach(post -> {
					if (post.get("userId").asInt() == user.get("id").asInt()) {
						String title = post.get("title").asText();
						// Print user information and corresponding post title
						System.out.println("Username: " + username);
						System.out.println("Address: " + street + ", " + suite + ", " + city + ", " + zipcode);
						System.out.println("Title: " + title);
						System.out.println();
					}
				});
			});
		}).exceptionally(ex -> {
			// Handle exceptions occurred during fetching
			System.err.println("An error occurred: " + ex.getMessage());
			return null;
		}).join(); // Wait for both futures to complete
	}
}
