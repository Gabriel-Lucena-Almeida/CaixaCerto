package br.ufpb.caixacerto.movement;

import br.ufpb.caixacerto.common.enums.MovementNature;
import br.ufpb.caixacerto.common.enums.MovementType;
import br.ufpb.caixacerto.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movements", indexes = {
    @Index(name = "idx_movements_user_date", columnList = "user_id, date DESC"),
    @Index(name = "idx_movements_user_type", columnList = "user_id, type"),
    @Index(name = "idx_movements_user_nature", columnList = "user_id, nature")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature", nullable = false, length = 10)
    private MovementNature nature;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private MovementType type;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "description", nullable = false, length = 80)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isEntrada() {
        return nature == MovementNature.ENTRADA;
    }

    public boolean isSaida() {
        return nature == MovementNature.SAIDA;
    }

    public boolean isNegocio() {
        return type == MovementType.NEGOCIO;
    }

    public boolean isPessoal() {
        return type == MovementType.PESSOAL;
    }
}