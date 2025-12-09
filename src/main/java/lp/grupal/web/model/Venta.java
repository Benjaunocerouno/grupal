package lp.grupal.web.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idventa;

    // Relación con Cliente
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Usuario usuario;

    // Relación con Vendedor
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    private Usuario empleado;

    // NUEVO: Relación con Empresa
    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_tipo_comprobante")
    private TipoComprobante tipoComprobante;

    private LocalDateTime fechaventa;
    private String estado;

    @Column(name = "serie_comprobante")
    private String serie_comprobante;

    @Column(name = "num_comprobante")
    private String num_comprobante;

    private Double monto_total;

    // Campos de envío (opcionales)
    @Column(name = "direccion_envio")
    private String direccionEnvio;
    
    @Column(name = "ciudad_envio")
    private String ciudadEnvio;
    
    @Column(name = "telefono_contacto")
    private String telefonoContacto;

    @PrePersist
    public void prePersist() {
        fechaventa = LocalDateTime.now();
        // Valor por defecto si no se asigna antes
        if (estado == null) estado = "PENDIENTE";
        
    }

    public Integer getIdventa() {
        return idventa;
    }

    public void setIdventa(Integer idventa) {
        this.idventa = idventa;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Usuario empleado) {
        this.empleado = empleado;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }
    
    public LocalDateTime getFechaventa() {
        return fechaventa;
    }

    public void setFechaventa(LocalDateTime fechaventa) {
        this.fechaventa = fechaventa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSerie_comprobante() {
        return serie_comprobante;
    }

    public void setSerie_comprobante(String serie_comprobante) {
        this.serie_comprobante = serie_comprobante;
    }

    public String getNum_comprobante() {
        return num_comprobante;
    }

    public void setNum_comprobante(String num_comprobante) {
        this.num_comprobante = num_comprobante;
    }

    public Double getMonto_total() {
        return monto_total;
    }

    public void setMonto_total(Double monto_total) {
        this.monto_total = monto_total;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getCiudadEnvio() {
        return ciudadEnvio;
    }

    public void setCiudadEnvio(String ciudadEnvio) {
        this.ciudadEnvio = ciudadEnvio;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    @Override
    public String toString() {
        return "Venta [idventa=" + idventa + ", usuario=" + usuario + ", empleado=" + empleado + ", empresa=" + empresa
                + ", tipoComprobante=" + tipoComprobante + ", fechaventa=" + fechaventa + ", estado=" + estado
                + ", serie_comprobante=" + serie_comprobante + ", num_comprobante=" + num_comprobante + ", monto_total="
                + monto_total + ", direccionEnvio=" + direccionEnvio + ", ciudadEnvio=" + ciudadEnvio
                + ", telefonoContacto=" + telefonoContacto + "]";
    }
}