package com.marketplace.ms_pedidos.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Entity @Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="usuario_id", nullable=false)
    private Long usuarioId;

    // Estados: PENDIENTE → PAGADO → ENVIADO → ENTREGADO → CANCELADO
    @Column(nullable=false, length=30)
    private String estado = "PENDIENTE";

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal total;

    @Column(name="direccion_entrega", nullable=false, length=300)
    private String direccionEntrega;

    @Column(name="creado_en", updatable=false)
    private LocalDateTime creadoEn;

    @Column(name="actualizado_en")
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy="pedido", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    @ToString.Exclude
    private List<ItemPedido> items;
    
    @PrePersist public void pre(){ creadoEn=actualizadoEn=LocalDateTime.now(); }
    @PreUpdate  public void upd(){ actualizadoEn=LocalDateTime.now(); }
}
