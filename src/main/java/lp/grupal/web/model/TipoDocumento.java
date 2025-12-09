package lp.grupal.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Integer idTipoDocumento;

    private String nombre;
    private String abreviatura;
    
    @Column(name = "longitud_min")
    private Integer longitudMin;
    
    @Column(name = "longitud_max")
    private Integer longitudMax;
    
    @Column(name = "es_numerico")
    private Boolean esNumerico; // true = solo numeros

    private Boolean estado;

    public Integer getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(Integer idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public Integer getLongitudMin() {
        return longitudMin;
    }

    public void setLongitudMin(Integer longitudMin) {
        this.longitudMin = longitudMin;
    }

    public Integer getLongitudMax() {
        return longitudMax;
    }

    public void setLongitudMax(Integer longitudMax) {
        this.longitudMax = longitudMax;
    }

    public Boolean getEsNumerico() {
        return esNumerico;
    }

    public void setEsNumerico(Boolean esNumerico) {
        this.esNumerico = esNumerico;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "TipoDocumento [idTipoDocumento=" + idTipoDocumento + ", nombre=" + nombre + ", abreviatura="
                + abreviatura + ", longitudMin=" + longitudMin + ", longitudMax=" + longitudMax + ", esNumerico="
                + esNumerico + ", estado=" + estado + "]";
    }

    
}