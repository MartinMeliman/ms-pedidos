package com.marketplace.ms_pedidos;

import com.marketplace.ms_pedidos.client.ProductoClient;
import com.marketplace.ms_pedidos.client.UsuarioClient;
import com.marketplace.ms_pedidos.dto.PedidoRequestDTO;
import com.marketplace.ms_pedidos.model.Pedido;
import com.marketplace.ms_pedidos.repository.PedidoRepository;
import com.marketplace.ms_pedidos.service.PedidoService;

import feign.FeignException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProductoClient productoClient;
    @Mock private UsuarioClient usuarioClient;
    @InjectMocks private PedidoService pedidoService;

    private PedidoRequestDTO dto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // DTO con un item: producto 1, cantidad 2
        PedidoRequestDTO.ItemDTO item = new PedidoRequestDTO.ItemDTO(1L, 2);
        dto = new PedidoRequestDTO(1L, "Av. Siempreviva 742", List.of(item));

        // Pedido de ejemplo
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuarioId(1L);
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(new BigDecimal("50000"));
    }

    // =========================================================================
    // TEST 1 — obtenerTodos: lista con pedidos
    // =========================================================================
    @Test
    @DisplayName("obtenerTodos: debería retornar lista de pedidos")
    void shouldReturnAllPedidos() {
        // GIVEN
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        // WHEN
        List<Pedido> resultado = pedidoService.obtenerTodos();
        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
    }

    // =========================================================================
    // TEST 2 — obtenerTodos: lista vacía
    // =========================================================================
    @Test
    @DisplayName("obtenerTodos: debería retornar lista vacía")
    void shouldReturnEmptyList() {
        // GIVEN
        when(pedidoRepository.findAll()).thenReturn(List.of());
        // WHEN
        List<Pedido> resultado = pedidoService.obtenerTodos();
        // THEN
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // =========================================================================
    // TEST 3 — obtenerPorId: pedido encontrado
    // =========================================================================
    @Test
    @DisplayName("obtenerPorId: debería retornar el pedido cuando existe")
    void shouldReturnPedidoById() {
        // GIVEN
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        // WHEN
        Optional<Pedido> resultado = pedidoService.obtenerPorId(1L);
        // THEN
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    // =========================================================================
    // TEST 4 — obtenerPorId: pedido no encontrado
    // =========================================================================
    @Test
    @DisplayName("obtenerPorId: debería retornar vacío cuando no existe")
    void shouldReturnEmptyWhenNotFound() {
        // GIVEN
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        // WHEN
        Optional<Pedido> resultado = pedidoService.obtenerPorId(99L);
        // THEN
        assertFalse(resultado.isPresent());
    }

    // =========================================================================
    // TEST 5 — crear: exitoso (usuario OK + producto OK + stock OK)
    // =========================================================================
    @Test
    @DisplayName("crear: debería crear el pedido cuando todo es válido")
    void shouldCreatePedidoSuccessfully() {
        // GIVEN — usuario existe, retorna un Map
        when(usuarioClient.obtenerPorId(1L)).thenReturn(Map.of("id", 1L));

        // Producto existe con stock 10 y precio 25000
        Map<String, Object> productoMock = Map.of(
            "id", 1L,
            "nombre", "Laptop",
            "precio", "25000",
            "stock", 10
        );
        when(productoClient.obtenerPorId(1L)).thenReturn(productoMock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // WHEN
        Pedido resultado = pedidoService.crear(dto);

        // THEN
        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstado());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // =========================================================================
    // TEST 6 — crear: usuario no existe → excepción
    // =========================================================================
    @Test
    @DisplayName("crear: debería lanzar excepción cuando el usuario no existe")
    void shouldThrowWhenUsuarioNotFound() {
        // GIVEN — usuarioClient lanza NotFound
        when(usuarioClient.obtenerPorId(1L))
            .thenThrow(FeignException.NotFound.class);

        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> pedidoService.crear(dto));

        assertTrue(ex.getMessage().contains("Usuario ID") ||
                   ex.getMessage().contains("no existe"));
        verify(pedidoRepository, never()).save(any());
    }

    // =========================================================================
    // TEST 7 — crear: producto no existe → excepción
    // =========================================================================
    @Test
    @DisplayName("crear: debería lanzar excepción cuando el producto no existe")
    void shouldThrowWhenProductoNotFound() {
        // GIVEN — usuario OK, producto lanza NotFound
        when(usuarioClient.obtenerPorId(1L)).thenReturn(Map.of("id", 1L));
        when(productoClient.obtenerPorId(1L))
            .thenThrow(FeignException.NotFound.class);

        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> pedidoService.crear(dto));

        assertTrue(ex.getMessage().contains("Producto ID") ||
                   ex.getMessage().contains("no existe"));
        verify(pedidoRepository, never()).save(any());
    }

    // =========================================================================
    // TEST 8 — crear: stock insuficiente → excepción
    // =========================================================================
    @Test
    @DisplayName("crear: debería lanzar excepción cuando el stock es insuficiente")
    void shouldThrowWhenStockInsuficiente() {
        // GIVEN — usuario OK, producto con stock=1, cantidad pedida=2
        when(usuarioClient.obtenerPorId(1L)).thenReturn(Map.of("id", 1L));

        Map<String, Object> productoStockBajo = Map.of(
            "id", 1L,
            "nombre", "Laptop",
            "precio", "25000",
            "stock", 1  // stock=1 < cantidad=2
        );
        when(productoClient.obtenerPorId(1L)).thenReturn(productoStockBajo);

        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> pedidoService.crear(dto));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(pedidoRepository, never()).save(any());
    }

    // =========================================================================
    // TEST 9 — actualizarEstado: estado válido
    // =========================================================================
    @Test
    @DisplayName("actualizarEstado: debería actualizar a ENVIADO correctamente")
    void shouldUpdateEstadoSuccessfully() {
        // GIVEN
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // WHEN
        Optional<Pedido> resultado = pedidoService.actualizarEstado(1L, "ENVIADO");

        // THEN
        assertTrue(resultado.isPresent());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // =========================================================================
    // TEST 10 — actualizarEstado: estado inválido → excepción
    // =========================================================================
    @Test
    @DisplayName("actualizarEstado: debería lanzar excepción con estado inválido")
    void shouldThrowWhenEstadoInvalido() {
        // GIVEN — "INVENTADO" no está en la lista de estados válidos
        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> pedidoService.actualizarEstado(1L, "INVENTADO"));

        assertTrue(ex.getMessage().contains("Estado invalido"));
        verify(pedidoRepository, never()).findById(anyLong());
    }

    // =========================================================================
    // TEST 11 — eliminar: pedido PENDIENTE → OK
    // =========================================================================
    @Test
    @DisplayName("eliminar: debería eliminar un pedido en estado PENDIENTE")
    void shouldDeletePedidoPendiente() {
        // GIVEN
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoRepository).deleteById(1L);

        // WHEN
        assertDoesNotThrow(() -> pedidoService.eliminar(1L));

        // THEN
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    // =========================================================================
    // TEST 12 — eliminar: pedido NO PENDIENTE → excepción
    // =========================================================================
    @Test
    @DisplayName("eliminar: no debería eliminar un pedido que no está PENDIENTE")
    void shouldThrowWhenDeletingNonPendiente() {
        // GIVEN — pedido en PAGADO no se puede eliminar
        Pedido pedidoPagado = new Pedido();
        pedidoPagado.setId(1L);
        pedidoPagado.setEstado("PAGADO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPagado));

        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> pedidoService.eliminar(1L));

        assertTrue(ex.getMessage().contains("PENDIENTE"));
        verify(pedidoRepository, never()).deleteById(anyLong());
    }
}