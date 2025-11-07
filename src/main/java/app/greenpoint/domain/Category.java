package app.greenpoint.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "esg_weight", nullable = false)
    private double esgWeight;
}
