package com.marketplace.ms_pedidos.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// Llama a ms-productos para verificar producto y descontar stock
@FeignClient(name="ms-productos", url="${ms.productos.url}")
public interface ProductoClient {
    @GetMapping("/api/productos/{id}") Map<String,Object> obtenerPorId(@PathVariable Long id);
    @PatchMapping("/api/productos/{id}/stock") void descontarStock(@PathVariable Long id, @RequestParam Integer cantidad);
}
