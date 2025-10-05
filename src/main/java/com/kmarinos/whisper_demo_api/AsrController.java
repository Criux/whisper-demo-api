package com.kmarinos.whisper_demo_api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1")
@Slf4j
public class AsrController {

  private static final String URL_SLOW = "http://192.168.1.71:9010";
  private static final String URL_FAST = "http://192.168.1.182:9000";

  @GetMapping("health")
  public ResponseEntity<Void> health() {
    return ResponseEntity.ok().build();
  }

  @PostMapping("asr")
  public ResponseEntity<String> asr(HttpServletRequest request, @RequestPart("audio_file") MultipartFile file) {
    String queryString = request.getQueryString();

    ResponseEntity<String> response = forwardRequest(URL_FAST, file, queryString);
    log.info("Got fast response:{}", response);
    if (response == null || !response.getStatusCode().is2xxSuccessful()) {
      response = forwardRequest(URL_SLOW, file, queryString);
      log.info("Got slow response:{}", response);
    }
    if (response == null) {
      return ResponseEntity.internalServerError().build();
    }
    return ResponseEntity.ok(response.getBody());
  }

  private ResponseEntity<String> forwardRequest(String url, MultipartFile file, String queryString) {
    log.info("Passing down request with to {} params:{}", url, queryString);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("audio_file", file.getResource());

    String serverUrl = url + "/asr?" + queryString;

    RestTemplate restTemplate = new RestTemplate();
    try {
      return restTemplate.postForEntity(serverUrl, body, String.class);
    } catch (Exception e) {
      log.warn("Error at downstream service: {}", e.getMessage());
    }
    return null;
  }
}
