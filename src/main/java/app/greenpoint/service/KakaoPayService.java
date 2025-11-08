package app.greenpoint.service;

import app.greenpoint.dto.kakaopay.KakaoPayApproveRequestDto;
import app.greenpoint.dto.kakaopay.KakaoPayApproveResponseDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyRequestDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Getter
public class KakaoPayService {

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.ready-url}")
    private String readyUrl;

    @Value("${kakaopay.approve-url}")
    private String approveUrl;

    @Value("${kakaopay.redirect-host}")
    private String redirectHost;

    @Value("${kakaopay.approval-path}")
    private String approvalPath;

    @Value("${kakaopay.cancel-path}")
    private String cancelPath;

    @Value("${kakaopay.fail-path}")
    private String failPath;

    private final RestTemplate restTemplate;

    public KakaoPayReadyResponseDto readyPayment(KakaoPayReadyRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        requestDto.setCid(cid);

        // Allow overriding redirect URLs from the request DTO
        if (!StringUtils.hasText(requestDto.getApproval_url())) {
            requestDto.setApproval_url(redirectHost + approvalPath);
        }
        if (!StringUtils.hasText(requestDto.getCancel_url())) {
            requestDto.setCancel_url(redirectHost + cancelPath);
        }
        if (!StringUtils.hasText(requestDto.getFail_url())) {
            requestDto.setFail_url(redirectHost + failPath);
        }


        HttpEntity<KakaoPayReadyRequestDto> request = new HttpEntity<>(requestDto, headers);

        return restTemplate.postForObject(readyUrl, request, KakaoPayReadyResponseDto.class);
    }

    public KakaoPayApproveResponseDto approvePayment(KakaoPayApproveRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        requestDto.setCid(cid);

        HttpEntity<KakaoPayApproveRequestDto> request = new HttpEntity<>(requestDto, headers);

        return restTemplate.postForObject(approveUrl, request, KakaoPayApproveResponseDto.class);
    }
}