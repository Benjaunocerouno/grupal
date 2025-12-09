package lp.grupal.web.model;

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
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idproducto;
    
    @ManyToOne
    @JoinColumn(name = "idcategoria")
    private Categoria categoria;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    private String nombre;
    private String descripcion;
    private String marca;
    private String modelo;
    private Double precio;
    
    private Integer stock;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "imagen_url")
    private String imagenUrl;

    private Boolean activo;

    // --- VALORES POR DEFECTO ---
    @PrePersist
    public void prePersist() {
        if(activo == null) activo = true;
        if(stock == null) stock = 0;
        if(stockMinimo == null) stockMinimo = 5;
    }

    // --- CONSTRUCTORES ---
    public Producto() {
    }

    public Producto(Integer idproducto) {
        this.idproducto = idproducto;
    }

    public Integer getIdproducto() {
        return idproducto;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public void setIdproducto(Integer idproducto) {
        this.idproducto = idproducto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Producto [idproducto=" + idproducto + ", categoria=" + categoria + ", codigoBarras=" + codigoBarras
                + ", empresa=" + empresa + ", nombre=" + nombre + ", descripcion=" + descripcion + ", marca=" + marca
                + ", modelo=" + modelo + ", precio=" + precio + ", stock=" + stock + ", stockMinimo=" + stockMinimo
                + ", imagenUrl=" + imagenUrl + ", activo=" + activo + "]";
    }

}