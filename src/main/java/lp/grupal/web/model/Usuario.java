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
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idusuario;

    private String username;
    private String contrasena;
    private String nombres;
    private String email;
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;
    
    @Column(name = "img_perfil")
    private String imgPerfil;
    
    // --- CORRECCIÓN AQUÍ: Cambiado de Boolean a Integer ---
    @Column(name = "estado")
    private Integer estado; // 1 = Activo, 0 = Inactivo

    @ManyToOne
    @JoinColumn(name = "idtipousuario")
    private TipoUsuario tipoUsuario;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        estado = 1; // Por defecto nace activo (1)
    }

    @ManyToOne
    @JoinColumn(name = "id_tipo_documento")
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "razon_social")
    private String razonSocial;
    
    @Column(name = "direccion_fiscal")
    private String direccionFiscal;

    
    
    public Integer getIdusuario() {
        return idusuario;
    }
    public void setIdusuario(Integer idusuario) {
        this.idusuario = idusuario;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    public String getNombres() {
        return nombres;
    }
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public Empresa getEmpresa() {
        return empresa;
    }
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
    public String getImgPerfil() {
        return imgPerfil;
    }
    public void setImgPerfil(String imgPerfil) {
        this.imgPerfil = imgPerfil;
    }
    public Integer getEstado() {
        return estado;
    }
    public void setEstado(Integer estado) {
        this.estado = estado;
    }
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    public String getRazonSocial() {
        return razonSocial;
    }
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
    public String getDireccionFiscal() {
        return direccionFiscal;
    }
    public void setDireccionFiscal(String direccionFiscal) {
        this.direccionFiscal = direccionFiscal;
    }
    @Override
    public String toString() {
        return "Usuario [idusuario=" + idusuario + ", username=" + username + ", contrasena=" + contrasena
                + ", nombres=" + nombres + ", email=" + email + ", telefono=" + telefono + ", empresa=" + empresa
                + ", imgPerfil=" + imgPerfil + ", estado=" + estado + ", tipoUsuario=" + tipoUsuario
                + ", fechaCreacion=" + fechaCreacion + ", tipoDocumento=" + tipoDocumento + ", numeroDocumento="
                + numeroDocumento + ", razonSocial=" + razonSocial + ", direccionFiscal=" + direccionFiscal + "]";
    }
}