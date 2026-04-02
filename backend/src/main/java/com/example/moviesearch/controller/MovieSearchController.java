package com.example.moviesearch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
public class MovieSearchController {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/search")
    public List<Map<String, Object>> searchMovies(@RequestBody Map<String, String> request) {
        String searchString = request.get("searchString");
        if (searchString == null || searchString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        boolean isJwt = apiKey != null && apiKey.startsWith("ey");
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.themoviedb.org/3/search/movie")
                .queryParam("query", searchString);

        if (!isJwt) {
            builder.queryParam("api_key", apiKey);
        }

        String url = builder.toUriString();
        HttpEntity<Void> entity = null;

        if (isJwt) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "application/json");
            entity = new HttpEntity<>(headers);
        }

        try {
            ResponseEntity<Map> responseEntity;
            if (isJwt) {
                responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            } else {
                responseEntity = restTemplate.getForEntity(url, Map.class);
            }

            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                
                return results.stream()
                        .limit(5)
                        .map(movie -> {
                            Map<String, Object> movieMap = new HashMap<>();
                            movieMap.put("title", movie.getOrDefault("title", "Unknown"));
                            movieMap.put("release_date", movie.getOrDefault("release_date", "N/A"));
                            movieMap.put("vote_average", movie.getOrDefault("vote_average", 0.0));
                            
                            Object posterPath = movie.get("poster_path");
                            if (posterPath != null) {
                                movieMap.put("poster_path", "https://image.tmdb.org/t/p/w500" + posterPath);
                            } else {
                                movieMap.put("poster_path", null);
                            }
                            
                            return movieMap;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error calling TMDB API: " + e.getMessage());
        }

        return new ArrayList<>();
    }
}
