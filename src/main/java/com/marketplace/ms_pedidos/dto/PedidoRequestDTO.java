package com.marketplace.ms_pedidos.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PedidoRequestDTO {
    @NotNull(message="El usuarioId es obligatorio")
    private Long usuarioId;

    @NotBlank(message="La direccion de entrega es obligatoria")
    private String direccionEntrega;
    
    @NotEmpty(message="El pedido debe tener al menos un item")
    private List<ItemDTO> items;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ItemDTO {
        @NotNull private Long productoId;
        @NotNull @Min(1) private Integer cantidad;
    }
}
