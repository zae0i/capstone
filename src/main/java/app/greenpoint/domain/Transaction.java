package app.greenpoint.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(nullable = false)
    private int amount;

    @Column(name = "tx_time", nullable = false)
    private LocalDateTime txTime;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "kakaopay_tid")
    private String tid;

    @Column(name = "kakaopay_aid")
    private String aid;

    @Column(name = "payment_method_type")
    private String paymentMethodType;

    public enum Source {
        MOCK, NAVERPAY, CARD_X, KAKAOPAY, WEB
    }

    public enum Status {
        PENDING, CONFIRMED, REJECTED
    }
}
