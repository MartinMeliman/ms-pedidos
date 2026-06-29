package com.marketplace.ms_pedidos.controller;
import com.marketplace.ms_pedidos.dto.PedidoRequestDTO;
import com.marketplace.ms_pedidos.model.Pedido;
import com.marketplace.ms_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/pedidos") @RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;
    @GetMapping public List<Pedido> obtenerTodos(){ return pedidoService.obtenerTodos(); }
    
    @GetMapping("/{id}") public ResponseEntity<Pedido> obtenerPorId(@PathVariable Long id){
        return pedidoService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @GetMapping("/usuario/{uid}") public List<Pedido> porUsuario(@PathVariable Long uid){
        return pedidoService.obtenerPorUsuario(uid); }

    @PostMapping public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequestDTO dto){
         return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(dto)); }

    @PutMapping("/{id}/estado") public ResponseEntity<Pedido> actualizarEstado(@PathVariable Long id, @RequestParam String estado){
         return pedidoService.actualizarEstado(id,estado).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());}

    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id){ pedidoService.eliminar(id); return ResponseEntity.noContent().build(); }
}
