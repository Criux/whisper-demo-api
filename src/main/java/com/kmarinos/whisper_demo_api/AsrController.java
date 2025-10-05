package com.kmarinos.whisper_demo_api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1")
@Slf4j
public class AsrController {

  @GetMapping("health")
  public ResponseEntity<Void> health(){
    return ResponseEntity.ok().build();
  }
  @PostMapping("asr")
  public ResponseEntity<String>asr(HttpServletRequest request,@RequestHeader HttpHeaders headers, @RequestParam Map<String,String> params, @RequestPart("audio_file")
      MultipartFile file){
    String queryString = request.getQueryString();
    log.info("Passing down request with params:{}",queryString);
    String response = forwardRequest(headers, file, queryString);
    log.info("Got response:{}",response);
    return ResponseEntity.ok(response);
  }
  private String forwardRequest(HttpHeaders headers, MultipartFile file, String queryString){
    MultiValueMap<String, Object> body
        = new LinkedMultiValueMap<>();
    body.add("audio_file",file.getResource());
    HttpEntity<MultiValueMap<String,Object>> requestEntity
        = new HttpEntity<>(body, headers);

    String serverUrl = "http://192.168.1.71:9010/asr?"+queryString;

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate
        .postForEntity(serverUrl, body, String.class);
    return response.getBody();
  }
}
