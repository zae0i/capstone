package app.greenpoint.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "merchant", indexes = {
    @Index(name = "idx_geo", columnList = "lat, lng"),
    @Index(name = "idx_region", columnList = "region")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(nullable = false, length = 60)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "esg_tier", nullable = false)
    private EsgTier esgTier;

    public enum EsgTier {
        A, B, C, D
    }
}
