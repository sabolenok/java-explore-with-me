package ru.practicum.explore_with_me.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explore_with_me.dto.Stat;
import ru.practicum.explore_with_me.dto.StatWithCount;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class StatsClient {

    @Value("${stats-server.url}")
    private String serverUrl;
    private final RestTemplate rest = new RestTemplate();

    public void hit(Stat stat) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Stat> requestEntity = new HttpEntity<>(stat, headers);

        rest.exchange(serverUrl + "/hit", HttpMethod.POST, requestEntity, Stat.class);
    }

    public List<StatWithCount> getStats(HttpServletRequest request) throws JsonProcessingException {
        String queryString = request.getQueryString();
        String queryUrl = serverUrl + "/stats" + (queryString.isBlank() ? "" : "?" + queryString);

        ResponseEntity<String> response = rest.getForEntity(URI.create(queryUrl), String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        StatWithCount[] array = objectMapper.readValue(response.getBody(), StatWithCount[].class);

        return Arrays.asList(array);
    }
}
