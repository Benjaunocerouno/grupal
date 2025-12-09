package lp.grupal.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle_compra")
    private Integer idDetalleCompra;

    @ManyToOne
    @JoinColumn(name = "idcompra")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "idproducto")
    private Producto producto;

    private Integer cantidad;

    @Column(name = "costo_unitario")
    private Double costoUnitario;

    // El subtotal es generado en BD, pero podemos tener el getter calculado
    public Double getSubtotal() {
        return (cantidad != null && costoUnitario != null) ? cantidad * costoUnitario : 0.0;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdDetalleCompra() { return idDetalleCompra; }
    public void setIdDetalleCompra(Integer idDetalleCompra) { this.idDetalleCompra = idDetalleCompra; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(Double costoUnitario) { this.costoUnitario = costoUnitario; }
}