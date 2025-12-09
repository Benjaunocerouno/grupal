package lp.grupal.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bancos_empresa")
public class BancosEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_banco")
    private Integer idBanco;

    @Column(name = "nombre_banco")
    private String nombreBanco;

    @Column(name = "numero_cuenta")
    private String numeroCuenta;

    private String cci;

    @Column(name = "nombre_titular")
    private String nombreTitular;

    private String moneda; // 'PEN' o 'USD'
    
    private Boolean activo;
    
    @Column(name = "imagen_qr")
    private String imagenQr;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Relación con Empresa (Muchos bancos pertenecen a 1 empresa)
    @ManyToOne
    @JoinColumn(name = "idempresa")
    private Empresa empresa;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        activo = true;
    }

    public Integer getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(Integer idBanco) {
        this.idBanco = idBanco;
    }

    public String getNombreBanco() {
        return nombreBanco;
    }

    public void setNombreBanco(String nombreBanco) {
        this.nombreBanco = nombreBanco;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getCci() {
        return cci;
    }

    public void setCci(String cci) {
        this.cci = cci;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getImagenQr() {
        return imagenQr;
    }

    public void setImagenQr(String imagenQr) {
        this.imagenQr = imagenQr;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "BancosEmpresa [idBanco=" + idBanco + ", nombreBanco=" + nombreBanco + ", numeroCuenta=" + numeroCuenta
                + ", cci=" + cci + ", nombreTitular=" + nombreTitular + ", moneda=" + moneda + ", activo=" + activo
                + ", imagenQr=" + imagenQr + ", fechaCreacion=" + fechaCreacion + ", empresa=" + empresa + "]";
    }
    
}