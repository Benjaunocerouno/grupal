package lp.grupal.web.model;
import jakarta.persistence.*;

@Entity
@Table(name = "tipopago")
public class TipoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idtipopago;
    private String nombre_tipo;
    public Integer getIdtipopago() {
        return idtipopago;
    }
    public void setIdtipopago(Integer idtipopago) {
        this.idtipopago = idtipopago;
    }
    public String getNombre_tipo() {
        return nombre_tipo;
    }
    public void setNombre_tipo(String nombre_tipo) {
        this.nombre_tipo = nombre_tipo;
    }
    @Override
    public String toString() {
        return "TipoPago [idtipopago=" + idtipopago + ", nombre_tipo=" + nombre_tipo + "]";
    }   
}