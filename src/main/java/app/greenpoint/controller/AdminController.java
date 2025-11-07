package app.greenpoint.controller;

import app.greenpoint.domain.Category;
import app.greenpoint.domain.EsgRule;
import app.greenpoint.domain.Merchant;
import app.greenpoint.dto.AdminCategoryDto;
import app.greenpoint.dto.AdminEsgRuleDto;
import app.greenpoint.dto.AdminMerchantDto;
import app.greenpoint.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // 이 컨트롤러의 모든 API에 보안 적용
public class AdminController {

    private final AdminService adminService;

    // ========== 가맹점 관리 ==========

    @Operation(summary = "모든 가맹점 조회")
    @GetMapping("/merchant")
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        return ResponseEntity.ok(adminService.getAllMerchants());
    }

    @Operation(summary = "ID로 특정 가맹점 조회")
    @GetMapping("/merchant/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMerchantById(id));
    }

    @Operation(summary = "새 가맹점 생성")
    @PostMapping("/merchant")
    public ResponseEntity<Merchant> createMerchant(@Valid @RequestBody AdminMerchantDto merchantDto) {
        Merchant createdMerchant = adminService.createMerchant(merchantDto);
        return new ResponseEntity<>(createdMerchant, HttpStatus.CREATED);
    }

    @Operation(summary = "기존 가맹점 정보 수정")
    @PutMapping("/merchant/{id}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @Valid @RequestBody AdminMerchantDto merchantDto) {
        Merchant updatedMerchant = adminService.updateMerchant(id, merchantDto);
        return ResponseEntity.ok(updatedMerchant);
    }

    @Operation(summary = "가맹점 삭제")
    @DeleteMapping("/merchant/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        adminService.deleteMerchant(id);
        return ResponseEntity.noContent().build();
    }

    // ========== 카테고리 관리 ==========

    @Operation(summary = "모든 카테고리 조회")
    @GetMapping("/category")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @Operation(summary = "새 카테고리 생성")
    @PostMapping("/category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody AdminCategoryDto categoryDto) {
        Category createdCategory = adminService.createCategory(categoryDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @Operation(summary = "기존 카테고리 정보 수정")
    @PutMapping("/category/{code}")
    public ResponseEntity<Category> updateCategory(@PathVariable String code, @Valid @RequestBody AdminCategoryDto categoryDto) {
        Category updatedCategory = adminService.updateCategory(code, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping("/category/{code}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String code) {
        adminService.deleteCategory(code);
        return ResponseEntity.noContent().build();
    }

    // ========== ESG 규칙 관리 ==========

    @Operation(summary = "모든 ESG 규칙 조회")
    @GetMapping("/esg-rule")
    public ResponseEntity<List<EsgRule>> getAllEsgRules() {
        return ResponseEntity.ok(adminService.getAllEsgRules());
    }

    @Operation(summary = "새 ESG 규칙 생성")
    @PostMapping("/esg-rule")
    public ResponseEntity<EsgRule> createEsgRule(@Valid @RequestBody AdminEsgRuleDto esgRuleDto) {
        EsgRule createdEsgRule = adminService.createEsgRule(esgRuleDto);
        return new ResponseEntity<>(createdEsgRule, HttpStatus.CREATED);
    }

    @Operation(summary = "기존 ESG 규칙 수정")
    @PutMapping("/esg-rule/{id}")
    public ResponseEntity<EsgRule> updateEsgRule(@PathVariable Long id, @Valid @RequestBody AdminEsgRuleDto esgRuleDto) {
        EsgRule updatedEsgRule = adminService.updateEsgRule(id, esgRuleDto);
        return ResponseEntity.ok(updatedEsgRule);
    }

    @Operation(summary = "ESG 규칙 삭제")
    @DeleteMapping("/esg-rule/{id}")
    public ResponseEntity<Void> deleteEsgRule(@PathVariable Long id) {
        adminService.deleteEsgRule(id);
        return ResponseEntity.noContent().build();
    }

    // ========== 배치 작업 트리거 ==========

    @Operation(summary = "랭킹 데이터 재계산", description = "특정 기간의 랭킹 데이터를 수동으로 재계산하는 배치 작업을 실행합니다.")
    @PostMapping("/ranking/rebuild")
    public ResponseEntity<String> rebuildRanking(@Parameter(description = "재계산할 기간 (예: 2025-10)") @RequestParam String period) {
        adminService.rebuildRanking(period);
        return ResponseEntity.ok("랭킹 재계산 작업이 요청되었습니다: " + period);
    }

    @Operation(summary = "리포트 데이터 재계산", description = "특정 기간의 리포트 데이터를 수동으로 재계산하는 배치 작업을 실행합니다.")
    @PostMapping("/report/rebuild")
    public ResponseEntity<String> rebuildReport(@Parameter(description = "재계산할 기간 (예: 2025-10)") @RequestParam String period) {
        adminService.rebuildReport(period);
        return ResponseEntity.ok("리포트 재계산 작업이 요청되었습니다: " + period);
    }
}
