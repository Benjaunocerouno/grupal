package lp.grupal.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcaja;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Column(name = "tipo_movimiento")
    private String tipoMovimiento; // 'INGRESO' o 'EGRESO'

    private String concepto;
    private Double monto;

    @Column(name = "metodo_pago")
    private String metodoPago;

    private String estado; // 'ACTIVO', 'ANULADO'

    // --- RELACIONES ---

    // ESTA ES LA QUE FALTABA Y CAUSABA EL ERROR
    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "id_solicitud_servicio")
    private SolicitudServicio solicitudServicio;

    @ManyToOne
    @JoinColumn(name = "id_usuario_responsable")
    private Usuario usuarioResponsable;

    // --- PRE-PERSIST (Valores por defecto) ---
    @PrePersist
    public void prePersist() {
        fechaMovimiento = LocalDateTime.now();
        if (estado == null) estado = "ACTIVO";
        if (tipoMovimiento == null) tipoMovimiento = "INGRESO";
    }

    public Integer getIdcaja() {
        return idcaja;
    }

    public void setIdcaja(Integer idcaja) {
        this.idcaja = idcaja;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public SolicitudServicio getSolicitudServicio() {
        return solicitudServicio;
    }

    public void setSolicitudServicio(SolicitudServicio solicitudServicio) {
        this.solicitudServicio = solicitudServicio;
    }

    public Usuario getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(Usuario usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
    }

    @Override
    public String toString() {
        return "Caja [idcaja=" + idcaja + ", fechaMovimiento=" + fechaMovimiento + ", tipoMovimiento=" + tipoMovimiento
                + ", concepto=" + concepto + ", monto=" + monto + ", metodoPago=" + metodoPago + ", estado=" + estado
                + ", empresa=" + empresa + ", pedido=" + pedido + ", venta=" + venta + ", solicitudServicio="
                + solicitudServicio + ", usuarioResponsable=" + usuarioResponsable + "]";
    }
    
}