package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private Optional<Serie> serieBuscada;

    private SerieRepository repository;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repository){
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 séries
                    7 - Buscar séries por categoria
                    8 - Filtrar séries
                    9 - Buscar episódio por trecho
                    10 - Top 5 episódios por série
                    11 - Episódios a partir de uma data
                   \s
                    0 - Sair                                \s
                   \s""";

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();
            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarAposData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Deseja buscar uma série por categoria/gênero?");
        var nomeGenero = leitura.nextLine();
        Categoria categoria  = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repository.findByGenero(categoria);
        System.out.println("Séries da categoria " + nomeGenero + ": ");
        seriesPorCategoria.forEach(System.out::println);
    }
    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

        List<DadosTemporada> temporadas = null;
        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(
                        ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

           List<Episodio> episodios = temporadas.stream().flatMap(d -> d.episodios().stream()
                    .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieBuscada = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome para a busca: ");
        var nomeDoAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor?");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas =
                repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeDoAtor, avaliacao);
        System.out.println("Séries em que " + nomeDoAtor + " trabalhou: ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));

    }

    private void buscarTop5Series() {
        List<Serie> serieTopCinco = repository.findTop5ByOrderByAvaliacaoDesc();
        serieTopCinco.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));;
    }

    private void filtrarSeriesPorTemporadaEAvaliacao() {
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> filtroSeries = repository.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(
                totalTemporadas, avaliacao);
        System.out.println("*** Séries filtradas ***");
        filtroSeries.forEach(s ->
                                     System.out.println(s.getTitulo() + "  - avaliação: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio para a busca?");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEcontrados = repository.episodioPorTrecho(trechoEpisodio);
        episodiosEcontrados.forEach(e -> System.out.printf("Série: %s | Temporada %s | Episódio %s - %s\n",
                                                            e.getSerie().getTitulo(), e.getTemporada(),
                                                            e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()){
           Serie serie = serieBuscada.get();
           List<Episodio> topEpisodio = repository.topEpisodiosPorSerie(serie);
            topEpisodio.forEach(e -> System.out.printf("Série: %s | Temporada %s | Episódio %s - %s\n",
                                                                   e.getSerie().getTitulo(), e.getTemporada(),
                                                                   e.getNumeroEpisodio(), e.getTitulo()));
        }
    }

    private void buscarAposData() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            System.out.println("Digite o ano limite de lançamento: ");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();
            List<Episodio> episodiosAno = repository.episodiosPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(e -> System.out.printf("Série: %s | Temporada %s | Episódio %s - %s - %s\n",
                                                       e.getSerie().getTitulo(), e.getTemporada(),
                                                       e.getNumeroEpisodio(), e.getTitulo(), e.getDataLancamento()));
        }
    }
}