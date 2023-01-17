package br.gov.planejamento.siconv.med.infra.message;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;

@RequestScoped
public class ResourceBundleMessages {

    private ResourceBundle bundle;

    @PostConstruct
    protected void init() {
        bundle = ResourceBundle.getBundle("messages");
    }

    public String getString(Message message) {
        return getString(message.getKey(), message.getArguments());
    }

    public String getString(MessageKey key) {
        return bundle.getString(key.value());
    }

    public String getString(String keyValue) {
        return bundle.getString(keyValue);
    }

    public String getString(MessageKey key, String... arguments) {
        return MessageFormat.format(bundle.getString(key.value()), (Object[]) arguments);
    }
}
