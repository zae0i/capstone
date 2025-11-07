package app.greenpoint.controller;

import app.greenpoint.dto.TransactionRequestDto;
import app.greenpoint.dto.TransactionResponseDto;
import app.greenpoint.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
