package com.marketplace.ms_pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "ms-usuarios", url = "${ms.usuarios.url}")
public interface UsuarioClient {
    @GetMapping("/api/usuarios/{id}")
    Map<String, Object> obtenerPorId(@PathVariable Long id);
}