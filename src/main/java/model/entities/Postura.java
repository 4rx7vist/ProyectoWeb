package model.entities;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Postura")
public class Postura implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Lob
    @Column(name = "instrucciones")
    private String instrucciones;

    @Lob
    @Column(name = "beneficios")
    private String beneficios;
    
    @Column(name = "duracion")
    private int duracion;

    @Column(name = "activa")
    private boolean activa;

    public Postura() {
        super();
        this.activa = true; // Valor por defecto
    }


    public Postura(String nombre, int duracion) {
        this();
        validarNombre(nombre);
        validarDuracion(duracion);
        this.nombre = nombre;
        this.duracion = duracion;
    }

    public Postura(String nombre, String fotoUrl, String videoUrl,
                   String instrucciones, String beneficios, int duracion, boolean activa) {
        this(nombre, duracion);
        this.fotoUrl = fotoUrl;
        this.videoUrl = videoUrl;
        this.instrucciones = instrucciones;
        this.beneficios = beneficios;
        this.activa = activa;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la postura no puede estar vacío");
        }
    }

    private void validarDuracion(int duracion) {
        if (duracion <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0");
        }
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public void setBeneficios(String beneficios) {
        this.beneficios = beneficios;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        validarDuracion(duracion);
        this.duracion = duracion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    // Patrón Builder (opcional pero recomendado)
    public static class Builder {
        private final String nombre;
        private final int duracion;
        private String fotoUrl;
        private String videoUrl;
        private String instrucciones;
        private String beneficios;
        private boolean activa = true;

        public Builder(String nombre, int duracion) {
            this.nombre = nombre;
            this.duracion = duracion;
        }

        public Builder fotoUrl(String fotoUrl) {
            this.fotoUrl = fotoUrl;
            return this;
        }

        public Builder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder instrucciones(String instrucciones) {
            this.instrucciones = instrucciones;
            return this;
        }

        public Builder beneficios(String beneficios) {
            this.beneficios = beneficios;
            return this;
        }

        public Builder activa(boolean activa) {
            this.activa = activa;
            return this;
        }

        public Postura build() {
            return new Postura(nombre, fotoUrl, videoUrl, instrucciones, beneficios, duracion, activa);
        }
    }
}
