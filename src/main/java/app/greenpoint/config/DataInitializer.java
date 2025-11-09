package app.greenpoint.config;

import app.greenpoint.domain.AppUser;
import app.greenpoint.domain.Category;
import app.greenpoint.domain.Merchant;
import app.greenpoint.repository.AppUserRepository;
import app.greenpoint.repository.CategoryRepository;
import app.greenpoint.repository.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    public CommandLineRunner initData(AppUserRepository appUserRepository, CategoryRepository categoryRepository, MerchantRepository merchantRepository) {
        return args -> {
            logger.info("============================================================");
            logger.info("========== [START] Data Initialization Logic ==========");
            logger.info("============================================================");

            // Admin User Initialization
            String adminEmail = "admin@greenpoint.app";
            appUserRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> {
                    if (user.getRole() != AppUser.Role.ADMIN) {
                        user.setRole(AppUser.Role.ADMIN);
                        appUserRepository.save(user);
                        logger.info(">>>> SUCCESS: Admin role set for user: {} <<<<", adminEmail);
                    } else {
                        logger.info(">>>> INFO: User {} already has ADMIN role. No changes made. <<<<", adminEmail);
                    }
                },
                () -> {
                    logger.warn(">>>> WARNING: Admin user with email {} not found. Please sign up with this email to grant admin privileges. <<<<", adminEmail);
                }
            );

            // Category Data Seeding
            List<Category> initialCategories = Arrays.asList(
                new Category("ECO", "친환경 매장", 1.2),
                new Category("VEGAN", "비건 식당", 1.5),
                new Category("RECYCLE", "재활용 센터", 1.3),
                new Category("LOCAL", "지역 상생", 1.1)
            );

            for (Category category : initialCategories) {
                if (!categoryRepository.existsById(category.getCategoryCode())) {
                    categoryRepository.save(category);
                    logger.info(">>>> INFO: Seeded category: {} - {}", category.getCategoryCode(), category.getName());
                } else {
                    logger.info(">>>> INFO: Category {} already exists. Skipping seeding.", category.getCategoryCode());
                }
            }

            // Merchant Data Seeding
            List<Merchant> initialMerchants = Arrays.asList(
                Merchant.builder()
                    .name("리오브리또")
                    .categoryCode("FNB")
                    .lat(new BigDecimal("37.336607795084"))
                    .lng(new BigDecimal("127.25165213598"))
                    .region("경기 용인시 처인구 모현읍")
                    .esgTier(Merchant.EsgTier.B)
                    .build(),
                Merchant.builder()
                    .name("한국외대 상가·맘스터치")
                    .categoryCode("CAMPUS")
                    .lat(new BigDecimal("37.335523563068"))
                    .lng(new BigDecimal("127.25862817578"))
                    .region("경기 용인시 처인구 모현읍")
                    .esgTier(Merchant.EsgTier.A)
                    .build(),
                Merchant.builder()
                    .name("디저트39 용인외대점")
                    .categoryCode("CAFE")
                    .lat(new BigDecimal("37.33622"))
                    .lng(new BigDecimal("127.25495"))
                    .region("경기 용인시 처인구 모현읍")
                    .esgTier(Merchant.EsgTier.B)
                    .build()
            );

            for (Merchant merchant : initialMerchants) {
                if (merchantRepository.findByName(merchant.getName()).isEmpty()) {
                    merchantRepository.save(merchant);
                    logger.info(">>>> INFO: Seeded merchant: {} - {}", merchant.getName(), merchant.getCategoryCode());
                } else {
                    logger.info(">>>> INFO: Merchant {} already exists. Skipping seeding.", merchant.getName());
                }
            }


            logger.info("============================================================");
            logger.info("=========== [END] Data Initialization Logic ===========");
            logger.info("============================================================");
        };
    }
}
