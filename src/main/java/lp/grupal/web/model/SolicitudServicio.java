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
@Table(name = "solicitud_servicio")
public class SolicitudServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idsolicitud;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idservicio_base")
    private Servicio servicioBase;

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_cita")
    private LocalDateTime fechaCita;

    @Column(name = "dispositivo_nombre")
    private String dispositivoNombre;

    @Column(name = "descripcion_problema")
    private String descripcionProblema;

    private String estado;

    @PrePersist
    public void prePersist() {
        fechaSolicitud = LocalDateTime.now();
        estado = "PENDIENTE";
    }

    public Integer getIdsolicitud() {
        return idsolicitud;
    }

    public void setIdsolicitud(Integer idsolicitud) {
        this.idsolicitud = idsolicitud;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Servicio getServicioBase() {
        return servicioBase;
    }

    public void setServicioBase(Servicio servicioBase) {
        this.servicioBase = servicioBase;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(LocalDateTime fechaCita) {
        this.fechaCita = fechaCita;
    }

    public String getDispositivoNombre() {
        return dispositivoNombre;
    }

    public void setDispositivoNombre(String dispositivoNombre) {
        this.dispositivoNombre = dispositivoNombre;
    }

    public String getDescripcionProblema() {
        return descripcionProblema;
    }

    public void setDescripcionProblema(String descripcionProblema) {
        this.descripcionProblema = descripcionProblema;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "SolicitudServicio [idsolicitud=" + idsolicitud + ", usuario=" + usuario + ", servicioBase="
                + servicioBase + ", empresa=" + empresa + ", fechaSolicitud=" + fechaSolicitud + ", fechaCita="
                + fechaCita + ", dispositivoNombre=" + dispositivoNombre + ", descripcionProblema="
                + descripcionProblema + ", estado=" + estado + "]";
    }
}