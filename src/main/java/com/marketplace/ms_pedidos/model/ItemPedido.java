package com.marketplace.ms_pedidos.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data @NoArgsConstructor @AllArgsConstructor @Entity @Table(name="items_pedido")
public class ItemPedido {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="producto_id", nullable=false)
    private Long productoId;

    @Column(name="nombre_producto", nullable=false, length=200)
    private String nombreProducto;

    @Column(nullable=false)
    private Integer cantidad;

    @Column(name="precio_unitario", nullable=false, precision=10, scale=2)
    private BigDecimal precioUnitario;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal subtotal;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pedido_id", nullable=false) @JsonIgnore
    private Pedido pedido;
}
