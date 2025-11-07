package app.greenpoint.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esg_rule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsgRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(name = "condition_json", columnDefinition = "TEXT")
    private String conditionJson;

    @Column(name = "score_formula")
    private String scoreFormula;
}
