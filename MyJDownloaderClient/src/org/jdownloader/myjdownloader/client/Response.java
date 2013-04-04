package org.jdownloader.myjdownloader.client;

import org.jdownloader.myjdownloader.client.json.Data;

public class Response {
    public Response(/* keep empty constructor json */) {

    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private Data   data;
    private String type;
}
