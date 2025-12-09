package lp.grupal.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idproveedor;

    private String ruc;
    
    @Column(name = "razon_social")
    private String razonSocial;
    
    private String telefono;
    private String email;
    private String direccion;
    
    private Boolean activo;

    // Relación con la empresa (para saber de quién es este proveedor)
    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @PrePersist
    public void prePersist() {
        if(activo == null) activo = true;
    }

    public Integer getIdproveedor() {
        return idproveedor;
    }

    public void setIdproveedor(Integer idproveedor) {
        this.idproveedor = idproveedor;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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
        return "Proveedor [idproveedor=" + idproveedor + ", ruc=" + ruc + ", razonSocial=" + razonSocial + ", telefono="
                + telefono + ", email=" + email + ", direccion=" + direccion + ", activo=" + activo + ", empresa="
                + empresa + "]";
    }
}