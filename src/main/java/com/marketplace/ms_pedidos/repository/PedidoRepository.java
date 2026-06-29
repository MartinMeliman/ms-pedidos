package com.marketplace.ms_pedidos.repository;
import com.marketplace.ms_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByEstado(String estado);
}
