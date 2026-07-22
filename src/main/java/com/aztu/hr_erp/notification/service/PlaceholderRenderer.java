package com.aztu.hr_erp.notification.service;

import java.util.Map;

/** Resolves {{name}}, {{vacancy}}, {{reason}}, … placeholders at send time. */
public final class PlaceholderRenderer {
    private PlaceholderRenderer() {}

    public static String render(String template, Map<String, String> vars) {
        if (template == null) return "";
        String out = template;
        if (vars != null) {
            for (Map.Entry<String, String> e : vars.entrySet()) {
                out = out.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
            }
        }
        return out;
    }
}
