package lp.grupal.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_comprobante")
public class TipoComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_comprobante")
    private Integer idTipoComprobante;

    private String nombre; // BOLETA, FACTURA
    
    @Column(name = "serie_default")
    private String serieDefault;
    
    @Column(name = "correlativo_actual")
    private Integer correlativoActual;

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    public Integer getIdTipoComprobante() {
        return idTipoComprobante;
    }

    public void setIdTipoComprobante(Integer idTipoComprobante) {
        this.idTipoComprobante = idTipoComprobante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSerieDefault() {
        return serieDefault;
    }

    public void setSerieDefault(String serieDefault) {
        this.serieDefault = serieDefault;
    }

    public Integer getCorrelativoActual() {
        return correlativoActual;
    }

    public void setCorrelativoActual(Integer correlativoActual) {
        this.correlativoActual = correlativoActual;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "TipoComprobante [idTipoComprobante=" + idTipoComprobante + ", nombre=" + nombre + ", serieDefault="
                + serieDefault + ", correlativoActual=" + correlativoActual + ", empresa=" + empresa + "]";
    }
    
    
}