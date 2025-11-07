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

import java.security.Principal;

@Tag(name = "Transactions", description = "Transaction & Rewards APIs")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Submit a transaction",
               description = "Submits a user's transaction to calculate ESG score and earn points. Requires authentication.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<TransactionResponseDto> submitTransaction(
            @Valid @RequestBody TransactionRequestDto transactionRequestDto,
            Authentication authentication) {

        // Get email from the authenticated principal
        String userEmail = authentication.getName();

        TransactionResponseDto response = transactionService.processTransaction(userEmail, transactionRequestDto);
        return ResponseEntity.ok(response);
    }
}
