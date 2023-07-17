package org.novomax.llm.integration.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.novomax.llm.integration.AiService;
import org.novomax.llm.integration.SearchResult;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@RestController
public class SearchController {

    private final AiService aiService;
    private final UiEntityUrlConfiguration uiEntityUrlConfiguration;

    SearchController(AiService aiService, UiEntityUrlConfiguration uiEntityUrlConfiguration) {
        this.aiService = aiService;
        this.uiEntityUrlConfiguration = uiEntityUrlConfiguration;
    }

    @GetMapping(value = "/llm-integration/ui/search-one", produces = MediaType.TEXT_HTML_VALUE)
    public String getSearchForm() {
        return readResourceAsString("llm-search-form.html");
    }

    @PostMapping("/llm-integration/ui/search-one")
    public void processSearch(@RequestParam("search-text") String searchText, HttpServletResponse response,
                              HttpServletRequest request) throws IOException {
        List<SearchResult> searchResultList = aiService.findByFreeText(searchText, 1);

        SearchResult searchResult = searchResultList.get(0);
        String urlTemplate = uiEntityUrlConfiguration.getSearchOneMapping().get(searchResult.entityClass());
        String urlPartToShowEntity = new MessageFormat(urlTemplate).format(new String[]{searchResult.entityId()});
        response.sendRedirect(getBaseUrlPart(request) + urlPartToShowEntity);
    }

    private String getBaseUrlPart(HttpServletRequest request) {
        if (request.getContextPath() == null || "".equals(request.getContextPath())) {
            return String.format("%s://%s:%s", request.getScheme(), request.getServerName(), request.getServerPort());
        }
        return String.format("%s://%s:%s/%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
    }

    private String readResourceAsString(String resourceName) {
        try {
            byte[] fileData = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream(resourceName));
            return new String(fileData);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }
}
