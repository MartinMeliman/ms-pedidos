package com.marketplace.ms_pedidos.service;
import com.marketplace.ms_pedidos.client.ProductoClient;
import com.marketplace.ms_pedidos.client.UsuarioClient;
import com.marketplace.ms_pedidos.dto.PedidoRequestDTO;
import com.marketplace.ms_pedidos.model.*;
import com.marketplace.ms_pedidos.repository.PedidoRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
//import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProductoClient productoClient;
    private final UsuarioClient usuarioClient;

    public List<Pedido> obtenerTodos(){ return pedidoRepository.findAll(); }
    public Optional<Pedido> obtenerPorId(Long id){ return pedidoRepository.findById(id); }
    public List<Pedido> obtenerPorUsuario(Long uid){ return pedidoRepository.findByUsuarioId(uid); }

    public Pedido crear(PedidoRequestDTO dto){
        validarUsuario(dto.getUsuarioId());
        log.info("Creando pedido para usuario ID: {}", dto.getUsuarioId());
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(dto.getUsuarioId());
        pedido.setDireccionEntrega(dto.getDireccionEntrega());
        pedido.setEstado("PENDIENTE");
        List<ItemPedido> items = new ArrayList<>(); BigDecimal total = BigDecimal.ZERO;
        for(PedidoRequestDTO.ItemDTO i : dto.getItems()){
            Map<String,Object> prod = validarProducto(i.getProductoId());
            //valida el stock
             Integer stockDisponible = ((Number) prod.get("stock")).intValue();
                if (stockDisponible < i.getCantidad())
                    throw new RuntimeException("Stock insuficiente para producto ID " 
                        + i.getProductoId() + ". Disponible: " + stockDisponible);

            BigDecimal precio = new BigDecimal(prod.get("precio").toString());
            BigDecimal sub = precio.multiply(BigDecimal.valueOf(i.getCantidad()));

            ItemPedido item = new ItemPedido();
            item.setProductoId(i.getProductoId());
            item.setNombreProducto((String)prod.get("nombre"));
            item.setCantidad(i.getCantidad());
            item.setPrecioUnitario(precio); item.setSubtotal(sub);
            item.setPedido(pedido);
            items.add(item); total = total.add(sub);
        }
        pedido.setItems(items); pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);
        // Descontar stock en ms-productos via FeignClient
        dto.getItems().forEach(i -> {
            try{ productoClient.descontarStock(i.getProductoId(),i.getCantidad()); 
                
            }catch(FeignException e){ log.warn("No se pudo descontar stock: {}",e.getMessage()); }});
        log.info("Pedido creado ID: {}, Total: {}", guardado.getId(), total);
        return guardado;
    }

    private void validarUsuario(Long usuarioId){
        if (usuarioId == null) return;
        try {
            usuarioClient.obtenerPorId(usuarioId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Usuario ID " + usuarioId + " no existe");
        } catch (FeignException e) {
            throw new RuntimeException("No se puede conectar con ms-usuarios");
        }
    }

    public Optional<Pedido> actualizarEstado(Long id, String estado){
        
        List<String> estadosValidos = List.of("PENDIENTE","PAGADO","ENVIADO","ENTREGADO","CANCELADO");
        if (!estadosValidos.contains(estado))
            throw new RuntimeException("Estado invalido: " + estado);

        return pedidoRepository.findById(id).map(p -> {
            if("CANCELADO".equals(estado)&&!"PENDIENTE".equals(p.getEstado())) throw new RuntimeException("Solo se puede cancelar un pedido PENDIENTE");
                p.setEstado(estado);
            return pedidoRepository.save(p);
        });
    }

    public void eliminar(Long id){
        Pedido p = pedidoRepository.findById(id).orElseThrow(()->new RuntimeException("Pedido no encontrado"));
        if(!"PENDIENTE".equals(p.getEstado())) throw new RuntimeException("Solo se puede eliminar un pedido PENDIENTE");
        pedidoRepository.deleteById(id);
    }

    private Map<String,Object> validarProducto(Long pid){
        try{ return productoClient.obtenerPorId(pid); }
        catch(FeignException.NotFound e){ throw new RuntimeException("Producto ID "+pid+" no existe"); }
        catch(FeignException e){ throw new RuntimeException("No se puede conectar con ms-productos"); }
    }
}
