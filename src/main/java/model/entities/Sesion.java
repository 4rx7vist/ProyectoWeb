package model.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Sesion")
public class Sesion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "dolor_inicial")
    private String dolorInicial;

    @Column(name = "dolor_final")
    private String dolorFinal;

    @Lob
    @Column(name = "comentario")
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "serie_id")
    private Serie serie;

    public Sesion() {}
    // Constructor, getters y setters...

	public Sesion(Long id, Paciente paciente, Date fecha, String dolorInicial, String dolorFinal, String comentario,
			Serie serie) {
		super();
		this.id = id;
		this.paciente = paciente;
		this.fecha = fecha;
		this.dolorInicial = dolorInicial;
		this.dolorFinal = dolorFinal;
		this.comentario = comentario;
		this.serie = serie;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Paciente getPaciente() {
	    return paciente;
	}

	public void setPaciente(Paciente paciente) {
	    this.paciente = paciente;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getDolorInicial() {
		return dolorInicial;
	}

	public void setDolorInicial(String dolorInicial) {
		this.dolorInicial = dolorInicial;
	}

	public String getDolorFinal() {
		return dolorFinal;
	}

	public void setDolorFinal(String dolorFinal) {
		this.dolorFinal = dolorFinal;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Serie getSerie() {
		return serie;
	}

	public void setSerie(Serie serie) {
		this.serie = serie;
	}
    
}
