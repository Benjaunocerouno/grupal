package lp.grupal.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcompra;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;

    @Column(name = "numero_comprobante")
    private String numeroComprobante;

    @Column(name = "monto_total")
    private Double montoTotal;

    private String estado;

    // --- RELACIONES ---

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    // --- CORRECCIÓN AQUÍ ---
    // El error ocurría porque antes decía "id_usuario".
    // Según tu base de datos, la columna se llama "id_usuario_responsable".
    @ManyToOne
    @JoinColumn(name = "id_usuario_responsable")
    private Usuario usuario; 

    @PrePersist
    public void prePersist() {
        fechaCompra = LocalDateTime.now();
        if (estado == null) {
            estado = "COMPLETADO"; // Valor por defecto según tu SQL
        }
    }

    public Integer getIdcompra() {
        return idcompra;
    }

    public void setIdcompra(Integer idcompra) {
        this.idcompra = idcompra;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
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

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "Compra [idcompra=" + idcompra + ", fechaCompra=" + fechaCompra + ", numeroComprobante="
                + numeroComprobante + ", montoTotal=" + montoTotal + ", estado=" + estado + ", empresa=" + empresa
                + ", proveedor=" + proveedor + ", usuario=" + usuario + "]";
    }
}