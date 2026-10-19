package com.ecowatt.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa um evento de uso registrado pelo sistema. Pode ser de dois tipos:
 *  - API    : uma requisição interceptada (endpoint + método + status);
 *  - CLIQUE : um clique de interface no front (rotulo + pagina).
 *
 * As duas fontes convivem na mesma tabela e alimentam o mapa de calor e os
 * rankings de uso. É a camada "bronze" (dado cru) do pipeline de analytics.
 */
@Entity
@Table(name = "evento_uso")
public class EventoUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pode ser null para requisições anônimas (ex.: login, cadastro).
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // "API" ou "CLIQUE".
    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String metodo;

    @Column(name = "status_http")
    private Integer statusHttp;

    // Preenchidos apenas quando tipoEvento = "CLIQUE".
    @Column(name = "rotulo")
    private String rotulo;

    @Column(name = "pagina")
    private String pagina;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public EventoUso() {}

    /** Construtor para eventos de API (requisições interceptadas). */
    public EventoUso(Usuario usuario, String endpoint, String metodo,
                     Integer statusHttp, LocalDateTime dataHora) {
        this.usuario = usuario;
        this.tipoEvento = "API";
        this.endpoint = endpoint;
        this.metodo = metodo;
        this.statusHttp = statusHttp;
        this.dataHora = dataHora;
    }

    /**
     * Fábrica para eventos de clique de interface. O endpoint/método guardam a
     * própria rota de clique para manter a coluna NOT NULL preenchida.
     */
    public static EventoUso clique(Usuario usuario, String rotulo,
                                   String pagina, LocalDateTime dataHora) {
        EventoUso e = new EventoUso();
        e.usuario = usuario;
        e.tipoEvento = "CLIQUE";
        e.endpoint = "/analytics/evento-clique";
        e.metodo = "POST";
        e.statusHttp = 200;
        e.rotulo = rotulo;
        e.pagina = pagina;
        e.dataHora = dataHora;
        return e;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public Integer getStatusHttp() { return statusHttp; }
    public void setStatusHttp(Integer statusHttp) { this.statusHttp = statusHttp; }

    public String getRotulo() { return rotulo; }
    public void setRotulo(String rotulo) { this.rotulo = rotulo; }

    public String getPagina() { return pagina; }
    public void setPagina(String pagina) { this.pagina = pagina; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
