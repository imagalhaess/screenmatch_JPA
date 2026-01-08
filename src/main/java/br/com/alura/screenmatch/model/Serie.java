package br.com.alura.screenmatch.model;
import br.com.alura.screenmatch.service.ConverteDados;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "series")
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;

    private Integer totalTemporadas;

    private Double avaliacao;

    @Enumerated(EnumType.STRING)
    private Categoria genero;

    private String atores;

    private String poster;

    private String sinopse;


    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Episodio> episodios = new ArrayList<>();

    public Serie(){}

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();
        // 1. Para evitar erro de estourado a partir de retorno 'null'
        this.avaliacao = Optional.ofNullable(dadosSerie.avaliacao())
                .filter(a -> !a.equalsIgnoreCase("N/A"))
                .map(Double::valueOf)
                .orElse(0.0);
        if (dadosSerie.genero() != null && !dadosSerie.genero().equalsIgnoreCase("N/A")) {
            this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
        } else {
            this.genero = null; // Ou uma categoria padrão como Categoria.DESCONHECIDO
        }
        this.atores = dadosSerie.atores();
        this.poster = dadosSerie.poster();
        this.sinopse = ConverteDados.obterTraducaoComFallback(dadosSerie.sinopse().trim());
    }

    public List<Episodio> getEpisodios() {
        return episodios;
    }

    public void setEpisodios(List<Episodio> episodios) {
        episodios.forEach(e -> e.setSerie(this));
        this.episodios = episodios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }

    public void setTotalTemporadas(Integer totalTemporadas) {
        this.totalTemporadas = totalTemporadas;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Categoria getGenero() {
        return (Categoria) genero;
    }

    public void setGenero(Categoria genero) {
        this.genero = genero;
    }

    public String getAtores() {
        return atores;
    }

    public void setAtores(String atores) {
        this.atores = atores;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    @Override
    public String toString() {
        return  "genero = " + genero +
                ", titulo='" + titulo + '\'' +
                ", totalTemporadas=" + totalTemporadas +
                ", avaliacao=" + avaliacao +
                ", atores='" + atores + '\'' +
                ", sinopse='" + sinopse + '\'';
    }

    private Double converterAvaliacao(String avaliacao) {
        try {
            return Double.valueOf(avaliacao);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }
}
