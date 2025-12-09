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
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idservicio;

    @Column(name = "idcategoria")
    private Integer idcategoria;

    private String nombre;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @Column(name = "precio_mano_obra")
    private Double precioManoObra;

    @Column(name = "tiempo_estimado_mins")
    private Integer tiempoEstimadoMins;

    @Column(name = "garantia_dias")
    private Integer garantiaDias;

    private Boolean activo;

    public Integer getIdservicio() {
        return idservicio;
    }

    public void setIdservicio(Integer idservicio) {
        this.idservicio = idservicio;
    }

    public Integer getIdcategoria() {
        return idcategoria;
    }

    public void setIdcategoria(Integer idcategoria) {
        this.idcategoria = idcategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecioManoObra() {
        return precioManoObra;
    }

    public void setPrecioManoObra(Double precioManoObra) {
        this.precioManoObra = precioManoObra;
    }

    public Integer getTiempoEstimadoMins() {
        return tiempoEstimadoMins;
    }

    public void setTiempoEstimadoMins(Integer tiempoEstimadoMins) {
        this.tiempoEstimadoMins = tiempoEstimadoMins;
    }

    public Integer getGarantiaDias() {
        return garantiaDias;
    }

    public void setGarantiaDias(Integer garantiaDias) {
        this.garantiaDias = garantiaDias;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "Servicio [idservicio=" + idservicio + ", idcategoria=" + idcategoria + ", nombre=" + nombre
                + ", descripcion=" + descripcion + ", empresa=" + empresa + ", precioManoObra=" + precioManoObra
                + ", tiempoEstimadoMins=" + tiempoEstimadoMins + ", garantiaDias=" + garantiaDias + ", activo=" + activo
                + "]";
    }
}
