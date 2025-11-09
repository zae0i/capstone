package app.greenpoint.controller;

import app.greenpoint.dto.MerchantResponseDto;
import app.greenpoint.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.greenpoint.dto.MerchantNameDto;
import java.util.List;

@Tag(name = "가맹점", description = "가맹점 정보 조회 API")
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "ID로 가맹점 이름 조회",
               description = "가맹점 ID를 통해 가맹점의 이름 정보를 조회합니다. 인증이 필요합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/name")
    public ResponseEntity<MerchantResponseDto> getMerchantNameById(@PathVariable Long id) {
        MerchantResponseDto merchant = merchantService.getMerchantById(id);
        return ResponseEntity.ok(merchant);
    }

    @Operation(summary = "모든 가맹점 이름 및 ID 조회",
               description = "드롭다운 메뉴 등에 사용될 모든 가맹점의 이름과 ID 목록을 조회합니다. 인증이 필요 없습니다.")
    @GetMapping("/names")
    public ResponseEntity<List<MerchantNameDto>> getAllMerchantNames() {
        List<MerchantNameDto> merchantNames = merchantService.getAllMerchantNames();
        return ResponseEntity.ok(merchantNames);
    }
}
