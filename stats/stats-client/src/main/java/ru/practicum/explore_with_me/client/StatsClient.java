package ru.practicum.explore_with_me.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import ru.practicum.explore_with_me.dto.Stat;
import ru.practicum.explore_with_me.dto.StatWithCount;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

    public List<StatWithCount> getStats(String start, String end, List<String> uris, Boolean unique)
            throws JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("uris", uris);
        parameters.put("unique", unique);

        ResponseEntity<String> response = rest.getForEntity(
                serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                String.class, parameters);

        ObjectMapper objectMapper = new ObjectMapper();
        StatWithCount[] array = objectMapper.readValue(response.getBody(), StatWithCount[].class);

        return Arrays.asList(array);
    }
}
