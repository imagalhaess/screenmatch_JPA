package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.service.traducao.ConsultaMyMemory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConverteDados implements IConverteDados {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T obterDados(String json, Class<T> classe) {
        try {
            return mapper.readValue(json, classe);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String obterTraducaoComFallback(String texto) {
        try {
            return ConsultaChatGPT.obterTraducao(texto).trim();
        } catch (Exception e) {
            try {
                return ConsultaMyMemory.obterTraducao(texto).trim();
            } catch (Exception ex) {
                return texto; // Retorna o original se tudo falhar
            }
        }
    }

}
