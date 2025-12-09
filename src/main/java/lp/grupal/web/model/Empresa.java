package lp.grupal.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idempresa;

    private String nombre;
    private String direccion;
    private String mision;
    private String vision;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "email_contacto")
    private String emailContacto;
    
    @Column(name = "telefono_mostrar")
    private String telefonoMostrar;
    
    @Column(name = "numero_whatsapp")
    private String numeroWhatsapp;

    public Integer getIdempresa() {
        return idempresa;
    }

    public void setIdempresa(Integer idempresa) {
        this.idempresa = idempresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getTelefonoMostrar() {
        return telefonoMostrar;
    }

    public void setTelefonoMostrar(String telefonoMostrar) {
        this.telefonoMostrar = telefonoMostrar;
    }

    public String getNumeroWhatsapp() {
        return numeroWhatsapp;
    }

    public void setNumeroWhatsapp(String numeroWhatsapp) {
        this.numeroWhatsapp = numeroWhatsapp;
    }

    public String getMision() {
        return mision;
    }

    public void setMision(String mision) {
        this.mision = mision;
    }

    public String getVision() {
        return vision;
    }

    public void setVision(String vision) {
        this.vision = vision;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @Override
    public String toString() {
        return "Empresa [idempresa=" + idempresa + ", nombre=" + nombre + ", direccion=" + direccion + ", mision="
                + mision + ", vision=" + vision + ", logoUrl=" + logoUrl + ", emailContacto=" + emailContacto
                + ", telefonoMostrar=" + telefonoMostrar + ", numeroWhatsapp=" + numeroWhatsapp + "]";
    }
    
    
}