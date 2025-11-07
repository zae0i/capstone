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

@Tag(name = "Admin", description = "Admin-only APIs")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // Apply security to all endpoints in this controller
public class AdminController {

    private final AdminService adminService;

    // ========== Merchant Management ==========

    @Operation(summary = "Get all merchants")
    @GetMapping("/merchant")
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        return ResponseEntity.ok(adminService.getAllMerchants());
    }

    @Operation(summary = "Get a single merchant by ID")
    @GetMapping("/merchant/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMerchantById(id));
    }

    @Operation(summary = "Create a new merchant")
    @PostMapping("/merchant")
    public ResponseEntity<Merchant> createMerchant(@Valid @RequestBody AdminMerchantDto merchantDto) {
        Merchant createdMerchant = adminService.createMerchant(merchantDto);
        return new ResponseEntity<>(createdMerchant, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing merchant")
    @PutMapping("/merchant/{id}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @Valid @RequestBody AdminMerchantDto merchantDto) {
        Merchant updatedMerchant = adminService.updateMerchant(id, merchantDto);
        return ResponseEntity.ok(updatedMerchant);
    }

    @Operation(summary = "Delete a merchant")
    @DeleteMapping("/merchant/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        adminService.deleteMerchant(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Category Management ==========

    @Operation(summary = "Get all categories")
    @GetMapping("/category")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @Operation(summary = "Create a new category")
    @PostMapping("/category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody AdminCategoryDto categoryDto) {
        Category createdCategory = adminService.createCategory(categoryDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing category")
    @PutMapping("/category/{code}")
    public ResponseEntity<Category> updateCategory(@PathVariable String code, @Valid @RequestBody AdminCategoryDto categoryDto) {
        Category updatedCategory = adminService.updateCategory(code, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @Operation(summary = "Delete a category")
    @DeleteMapping("/category/{code}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String code) {
        adminService.deleteCategory(code);
        return ResponseEntity.noContent().build();
    }

    // ========== ESG Rule Management ==========

    @Operation(summary = "Get all ESG rules")
    @GetMapping("/esg-rule")
    public ResponseEntity<List<EsgRule>> getAllEsgRules() {
        return ResponseEntity.ok(adminService.getAllEsgRules());
    }

    @Operation(summary = "Create a new ESG rule")
    @PostMapping("/esg-rule")
    public ResponseEntity<EsgRule> createEsgRule(@Valid @RequestBody AdminEsgRuleDto esgRuleDto) {
        EsgRule createdEsgRule = adminService.createEsgRule(esgRuleDto);
        return new ResponseEntity<>(createdEsgRule, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing ESG rule")
    @PutMapping("/esg-rule/{id}")
    public ResponseEntity<EsgRule> updateEsgRule(@PathVariable Long id, @Valid @RequestBody AdminEsgRuleDto esgRuleDto) {
        EsgRule updatedEsgRule = adminService.updateEsgRule(id, esgRuleDto);
        return ResponseEntity.ok(updatedEsgRule);
    }

    @Operation(summary = "Delete an ESG rule")
    @DeleteMapping("/esg-rule/{id}")
    public ResponseEntity<Void> deleteEsgRule(@PathVariable Long id) {
        adminService.deleteEsgRule(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Batch Job Triggers ==========

    @Operation(summary = "Trigger ranking rebuild", description = "Manually triggers the batch job to rebuild ranking data for a specific period.")
    @PostMapping("/ranking/rebuild")
    public ResponseEntity<String> rebuildRanking(@Parameter(description = "Period to rebuild (e.g., 2025-10)") @RequestParam String period) {
        adminService.rebuildRanking(period);
        return ResponseEntity.ok("Ranking rebuild job triggered for period: " + period);
    }

    @Operation(summary = "Trigger report rebuild", description = "Manually triggers the batch job to rebuild report data for a specific period.")
    @PostMapping("/report/rebuild")
    public ResponseEntity<String> rebuildReport(@Parameter(description = "Period to rebuild (e.g., 2025-10)") @RequestParam String period) {
        adminService.rebuildReport(period);
        return ResponseEntity.ok("Report rebuild job triggered for period: " + period);
    }
}
