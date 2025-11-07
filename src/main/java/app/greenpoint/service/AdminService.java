package app.greenpoint.service;

import app.greenpoint.domain.Category;
import app.greenpoint.domain.EsgRule;
import app.greenpoint.domain.Merchant;
import app.greenpoint.dto.AdminCategoryDto;
import app.greenpoint.dto.AdminEsgRuleDto;
import app.greenpoint.dto.AdminMerchantDto;
import app.greenpoint.repository.CategoryRepository;
import app.greenpoint.repository.EsgRuleRepository;
import app.greenpoint.repository.MerchantRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final EsgRuleRepository esgRuleRepository;

    // ========== Merchant Management ==========

    public Merchant createMerchant(AdminMerchantDto dto) {
        Merchant merchant = new Merchant();
        merchant.setName(dto.getName());
        merchant.setCategoryCode(dto.getCategoryCode());
        merchant.setLat(dto.getLat());
        merchant.setLng(dto.getLng());
        merchant.setRegion(dto.getRegion());
        merchant.setEsgTier(dto.getEsgTier());
        return merchantRepository.save(merchant);
    }

    public Merchant updateMerchant(Long merchantId, AdminMerchantDto dto) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found with id: " + merchantId));
        
        merchant.setName(dto.getName());
        merchant.setCategoryCode(dto.getCategoryCode());
        merchant.setLat(dto.getLat());
        merchant.setLng(dto.getLng());
        merchant.setRegion(dto.getRegion());
        merchant.setEsgTier(dto.getEsgTier());
        return merchantRepository.save(merchant);
    }

    public void deleteMerchant(Long merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new EntityNotFoundException("Merchant not found with id: " + merchantId);
        }
        merchantRepository.deleteById(merchantId);
    }

    @Transactional(readOnly = true)
    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Merchant getMerchantById(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found with id: " + merchantId));
    }

    // ========== Category Management ==========

    public Category createCategory(AdminCategoryDto dto) {
        if (categoryRepository.existsById(dto.getCategoryCode())) {
            throw new EntityExistsException("Category with code " + dto.getCategoryCode() + " already exists.");
        }
        Category category = new Category();
        category.setCategoryCode(dto.getCategoryCode());
        category.setName(dto.getName());
        category.setEsgWeight(dto.getEsgWeight());
        return categoryRepository.save(category);
    }

    public Category updateCategory(String categoryCode, AdminCategoryDto dto) {
        Category category = categoryRepository.findById(categoryCode)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with code: " + categoryCode));
        
        category.setName(dto.getName());
        category.setEsgWeight(dto.getEsgWeight());
        return categoryRepository.save(category);
    }

    public void deleteCategory(String categoryCode) {
        if (!categoryRepository.existsById(categoryCode)) {
            throw new EntityNotFoundException("Category not found with code: " + categoryCode);
        }
        categoryRepository.deleteById(categoryCode);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ========== ESG Rule Management ==========

    public EsgRule createEsgRule(AdminEsgRuleDto dto) {
        EsgRule esgRule = new EsgRule();
        esgRule.setName(dto.getName());
        esgRule.setConditionJson(dto.getConditionJson());
        esgRule.setScoreFormula(dto.getScoreFormula());
        return esgRuleRepository.save(esgRule);
    }

    public EsgRule updateEsgRule(Long ruleId, AdminEsgRuleDto dto) {
        EsgRule esgRule = esgRuleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("EsgRule not found with id: " + ruleId));
        
        esgRule.setName(dto.getName());
        esgRule.setConditionJson(dto.getConditionJson());
        esgRule.setScoreFormula(dto.getScoreFormula());
        return esgRuleRepository.save(esgRule);
    }

    public void deleteEsgRule(Long ruleId) {
        if (!esgRuleRepository.existsById(ruleId)) {
            throw new EntityNotFoundException("EsgRule not found with id: " + ruleId);
        }
        esgRuleRepository.deleteById(ruleId);
    }

    @Transactional(readOnly = true)
    public List<EsgRule> getAllEsgRules() {
        return esgRuleRepository.findAll();
    }

    // ========== Batch Job Triggers ==========

    public void rebuildRanking(String period) {
        logger.info("Manual ranking rebuild triggered for period: {}", period);
        // In a real application, this would publish an event or call a batch job service.
    }

    public void rebuildReport(String period) {
        logger.info("Manual report rebuild triggered for period: {}", period);
        // In a real application, this would publish an event or call a batch job service.
    }
}
