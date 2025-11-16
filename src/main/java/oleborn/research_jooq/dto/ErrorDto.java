package oleborn.research_jooq.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public record ErrorDto(
        int errorCode,
        String errorDescription,
        String nameMethod,
        String uri
) {

    /**
     * Выводит в лог объект ошибки в виде красивого json
     *
     * @param mapper - обьект маппера
     * @return красивый json
     */
    public String logAsJson(ObjectMapper mapper) {
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert errorDto to JSON", e);
        }
        return json;
    }
}