package app.greenpoint.service;

import app.greenpoint.dto.kakaopay.KakaoPayApproveRequestDto;
import app.greenpoint.dto.kakaopay.KakaoPayApproveResponseDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyRequestDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoPayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KakaoPayService kakaoPayService;

    private String secretKey = "test-secret-key";
    private String cid = "test-cid";
    private String readyUrl = "http://test-ready-url.com";
    private String approveUrl = "http://test-approve-url.com";
    private String redirectHost = "http://test-redirect-host.com";
    private String approvalPath = "/approval";
    private String cancelPath = "/cancel";
    private String failPath = "/fail";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kakaoPayService, "secretKey", secretKey);
        ReflectionTestUtils.setField(kakaoPayService, "cid", cid);
        ReflectionTestUtils.setField(kakaoPayService, "readyUrl", readyUrl);
        ReflectionTestUtils.setField(kakaoPayService, "approveUrl", approveUrl);
        ReflectionTestUtils.setField(kakaoPayService, "redirectHost", redirectHost);
        ReflectionTestUtils.setField(kakaoPayService, "approvalPath", approvalPath);
        ReflectionTestUtils.setField(kakaoPayService, "cancelPath", cancelPath);
        ReflectionTestUtils.setField(kakaoPayService, "failPath", failPath);
    }

    @Test
    void readyPayment_shouldReturnKakaoPayReadyResponseDto() {
        // Given
        KakaoPayReadyRequestDto requestDto = new KakaoPayReadyRequestDto();
        requestDto.setPartner_order_id("order123");
        requestDto.setPartner_user_id("user123");
        requestDto.setItem_name("itemA");
        requestDto.setQuantity(1);
        requestDto.setTotal_amount(1000);
        requestDto.setTax_free_amount(0);

        KakaoPayReadyResponseDto expectedResponse = new KakaoPayReadyResponseDto();
        expectedResponse.setTid("tid123");
        expectedResponse.setNext_redirect_pc_url("http://redirect.url");

        when(restTemplate.postForObject(eq(readyUrl), any(HttpEntity.class), eq(KakaoPayReadyResponseDto.class)))
                .thenReturn(expectedResponse);

        // When
        KakaoPayReadyResponseDto actualResponse = kakaoPayService.readyPayment(requestDto);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getTid(), actualResponse.getTid());
        assertEquals(expectedResponse.getNext_redirect_pc_url(), actualResponse.getNext_redirect_pc_url());

        assertEquals(cid, requestDto.getCid());
        assertEquals(redirectHost + approvalPath, requestDto.getApproval_url());
        assertEquals(redirectHost + cancelPath, requestDto.getCancel_url());
        assertEquals(redirectHost + failPath, requestDto.getFail_url());

        verify(restTemplate).postForObject(eq(readyUrl), any(HttpEntity.class), eq(KakaoPayReadyResponseDto.class));
    }

    @Test
    void approvePayment_shouldReturnKakaoPayApproveResponseDto() {
        // Given
        KakaoPayApproveRequestDto requestDto = new KakaoPayApproveRequestDto();
        requestDto.setTid("tid123");
        requestDto.setPartner_order_id("order123");
        requestDto.setPartner_user_id("user123");
        requestDto.setPg_token("pgtoken123");

        KakaoPayApproveResponseDto expectedResponse = new KakaoPayApproveResponseDto();
        expectedResponse.setAid("aid123");
        expectedResponse.setTid("tid123");

        when(restTemplate.postForObject(eq(approveUrl), any(HttpEntity.class), eq(KakaoPayApproveResponseDto.class)))
                .thenReturn(expectedResponse);

        // When
        KakaoPayApproveResponseDto actualResponse = kakaoPayService.approvePayment(requestDto);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getAid(), actualResponse.getAid());
        assertEquals(expectedResponse.getTid(), actualResponse.getTid());

        assertEquals(cid, requestDto.getCid());

        verify(restTemplate).postForObject(eq(approveUrl), any(HttpEntity.class), eq(KakaoPayApproveResponseDto.class));
    }
}
