package com.marketplace.ms_pedidos.controller;

import com.marketplace.ms_pedidos.dto.PedidoRequestDTO;
import com.marketplace.ms_pedidos.model.Pedido;
import com.marketplace.ms_pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Pedidos", description = "Gestión de pedidos del marketplace EcoTrade")
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(summary = "Listar todos los pedidos",
               description = "Retorna la lista completa de pedidos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    })
    @GetMapping
    public List<Pedido> obtenerTodos() {
        return pedidoService.obtenerTodos();
    }

    @Operation(summary = "Obtener pedido por ID",
               description = "Busca un pedido por su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPorId(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        return pedidoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener pedidos por usuario",
               description = "Retorna todos los pedidos de un usuario específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pedidos del usuario")
    })
    @GetMapping("/usuario/{uid}")
    public List<Pedido> porUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long uid) {
        return pedidoService.obtenerPorUsuario(uid);
    }

    @Operation(summary = "Crear nuevo pedido",
               description = "Crea un pedido validando usuario, productos y stock disponible. " +
                             "Lanza error si el usuario no existe, el producto no existe o el stock es insuficiente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuario o producto no existe")
    })
    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(dto));
    }

    @Operation(summary = "Actualizar estado del pedido",
               description = "Actualiza el estado. Estados válidos: PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO. " +
                             "Solo se puede cancelar un pedido en estado PENDIENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Estado inválido o no permitido"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "Nuevo estado: PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO")
            @RequestParam String estado) {
        return pedidoService.actualizarEstado(id, estado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar pedido",
               description = "Elimina un pedido. Solo se pueden eliminar pedidos en estado PENDIENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pedido eliminado correctamente"),
        @ApiResponse(responseCode = "400", description = "El pedido no está en estado PENDIENTE"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del pedido a eliminar") @PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}