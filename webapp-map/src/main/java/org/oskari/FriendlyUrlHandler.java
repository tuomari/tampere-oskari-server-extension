package org.oskari;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.spring.extension.OskariParam;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FriendlyUrlHandler {

    private String filedlUUID = null;

    @RequestMapping("/filedl")
    public String redirectToExample(Model model,
                                    @OskariParam ActionParameters params) throws Exception {
        return redirectToExample(PropertyUtil.getDefaultLanguage(), model, params);
    }

    @RequestMapping("/filedl/{lang}")
    public String redirectToExample(@PathVariable("lang") String lang,
                                    Model model,
                                    @OskariParam ActionParameters params) throws Exception {
        if(!isSupported(lang)) {
            lang = PropertyUtil.getDefaultLanguage();
        }

        String url = "/?lang=" + lang + "&uuid=" + getFileDL();
        return "redirect:" + attachQuery(url, params.getRequest().getQueryString());
    }

    private boolean isSupported(String lang) {
        if (lang == null || lang.isEmpty()) {
            return false;
        }
        for(String l: PropertyUtil.getSupportedLanguages()) {
            if(l.equalsIgnoreCase(lang)) {
                return true;
            }
        }
        return false;
    }

    private String attachQuery(String path, String query) {
        if(query == null) {
            return path;
        }
        if(path.indexOf('?') == -1) {
            return path + "?" + query;
        }
        if(path.endsWith("?")) {
            return path + query;
        }
        return path + "&" + query;

    }

    private String getFileDL() {
        if (filedlUUID != null) {
            return filedlUUID;
        }
        filedlUUID = OskariComponentManager
                .getComponentOfType(ViewService.class)
                .getViewWithConf("Latauspalvelu")
                .getUuid();
        return filedlUUID;
    }
}