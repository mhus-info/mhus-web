package de.mhus.cherry.web.util.webspace;

import java.util.LinkedList;
import java.util.List;

public class TypeDefinition {

    private String name;
    private String[] refs;
    private LinkedList<TypeHeader> headers = new LinkedList<>();
    private String mimeType;


    public void setReferences(String refs) {
        this.refs = null;
        if (refs == null) return;
        this.refs = refs.split(",");
    }

    public void setReferences(String[] refs) {
        this.refs = refs;
    }

    public String[] getReferences() {
        return refs;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void addHeader(String key, String value) {
        headers.add(new TypeHeader(key, value));
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<TypeHeader> getHeaders() {
        return headers;
    }


}
