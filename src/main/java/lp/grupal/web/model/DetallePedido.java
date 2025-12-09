package lp.grupal.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle_pedido") // Ojo: en tu imagen se llama así
    private Integer idDetallePedido;

    @ManyToOne
    @JoinColumn(name = "idpedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "idproducto")
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_referencial")
    private Double precioReferencial;

    // --- GETTERS Y SETTERS ---
    public Integer getIdDetallePedido() { return idDetallePedido; }
    public void setIdDetallePedido(Integer idDetallePedido) { this.idDetallePedido = idDetallePedido; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioReferencial() { return precioReferencial; }
    public void setPrecioReferencial(Double precioReferencial) { this.precioReferencial = precioReferencial; }
}