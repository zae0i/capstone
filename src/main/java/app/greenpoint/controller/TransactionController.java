package app.greenpoint.controller;

import app.greenpoint.dto.TransactionRequestDto;
import app.greenpoint.dto.TransactionResponseDto;
import app.greenpoint.dto.kakaopay.KakaoPayReadyResponseDto;
import app.greenpoint.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Tag(name = "거래", description = "거래 및 리워드 적립 API")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "거래 내역 제출",
               description = "사용자의 거래 내역을 제출하여 ESG 점수를 계산하고 포인트를 적립합니다. 인증이 필요합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<TransactionResponseDto> submitTransaction(
            @Valid @RequestBody TransactionRequestDto transactionRequestDto,
            Authentication authentication) {

        // 인증된 사용자 정보에서 이메일 가져오기
        String userEmail = authentication.getName();

        TransactionResponseDto response = transactionService.processTransaction(userEmail, transactionRequestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오페이 결제 준비",
               description = "카카오페이 결제를 시작하기 위한 정보를 요청하고, 결제 고유 번호와 리다이렉트 URL을 받습니다. 인증이 필요합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/kakao/ready")
    public ResponseEntity<KakaoPayReadyResponseDto> kakaoPayReady(
            @Valid @RequestBody TransactionRequestDto transactionRequestDto,
            Authentication authentication) {
        String userEmail = authentication.getName();
        KakaoPayReadyResponseDto response = transactionService.initiateKakaoPayPayment(userEmail, transactionRequestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오페이 결제 성공 콜백",
               description = "카카오페이 결제 성공 시 호출되는 콜백 URL입니다. pg_token을 받아 결제를 승인합니다.")
    @GetMapping("/kakao/success/{orderId}")
    public RedirectView kakaoPaySuccess(@RequestParam("pg_token") String pgToken,
                                        @PathVariable("orderId") Long orderId) {
        transactionService.approveKakaoPayPayment(pgToken, orderId);
        // Redirect to a frontend success page, passing the order ID
        // TODO: The base URL should be configurable
        return new RedirectView("http://localhost:5173/payment/success/" + orderId);
    }

    @Operation(summary = "카카오페이 결제 취소 콜백",
               description = "카카오페이 결제 취소 시 호출되는 콜백 URL입니다.")
    @GetMapping("/kakao/cancel")
    public RedirectView kakaoPayCancel() {
        // Redirect to a frontend cancel page
        return new RedirectView("http://localhost:5173/payment/cancel"); // TODO: Configure frontend cancel URL
    }

    @Operation(summary = "카카오페이 결제 실패 콜백",
               description = "카카오페이 결제 실패 시 호출되는 콜백 URL입니다.")
    @GetMapping("/kakao/fail")
    public RedirectView kakaoPayFail() {
        // Redirect to a frontend fail page
        return new RedirectView("http://localhost:5173/payment/fail"); // TODO: Configure frontend fail URL
    }
}