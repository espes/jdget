//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.http.requests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;

import jd.http.Browser;
import jd.http.Request;
import jd.http.URLConnectionAdapter;

import org.appwork.utils.net.CountingOutputStream;
import org.appwork.utils.net.NullOutputStream;
import org.appwork.utils.net.httpconnection.HTTPConnection.RequestMethod;

/**
 * Extending the Request class, this class is able to to HTML Formdata Posts.
 * 
 * @author coalado
 */
public class PostFormDataRequest extends Request {

    private String                    boundary;
    private final java.util.List<FormData> formDatas;
    private String                    encodeType = "multipart/form-data";

    public PostFormDataRequest(final String url) throws MalformedURLException {
        super(Browser.correctURL(url));
        this.generateBoundary();
        this.formDatas = new ArrayList<FormData>();
    }

    public void addFormData(final FormData fd) {
        this.formDatas.add(fd);
    }

    private void generateBoundary() {
        final long range = 999999999999999l - 100000000000000l;
        final long rand = (long) (Math.random() * range) + 100000000000000l;
        this.boundary = "---------------------" + rand;
    }

    public String getEncodeType() {
        return this.encodeType;
    }

    public String getPostDataString() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.formDatas.size(); i++) {
            this.write(this.formDatas.get(i), sb);
        }
        sb.append(this.boundary);
        sb.append("--\r\n");
        return sb.toString();
    }

    private long postContent(final URLConnectionAdapter httpConnection) throws IOException {
        CountingOutputStream output = null;
        if (httpConnection != null && httpConnection.getOutputStream() != null) {
            output = new CountingOutputStream(httpConnection.getOutputStream());
        } else {
            output = new CountingOutputStream(new NullOutputStream());
        }
        try {
            for (int i = 0; i < this.formDatas.size(); i++) {
                this.write(this.formDatas.get(i), output);
            }
            final OutputStreamWriter writer = new OutputStreamWriter(output);
            writer.write(this.boundary);
            writer.write("--\r\n");
            writer.flush();
            output.flush();
        } finally {
        }
        return output.transferedBytes();
    }

    /**
     * send the postData of the Request. in case httpConnection is null, it
     * outputs the data to a NullOutputStream
     */
    @Override
    public long postRequest() throws IOException {
        return this.postContent(this.httpConnection);
    }

    @Override
    public void preRequest() throws IOException {
        this.httpConnection.setRequestMethod(RequestMethod.POST);
        this.httpConnection.setRequestProperty("Content-Type", this.encodeType + "; boundary=" + this.boundary.substring(2));
        this.httpConnection.setRequestProperty("Content-Length", this.postContent(null) + "");
    }

    public void setEncodeType(final String encodeType) {
        this.encodeType = encodeType;
    }

    private void write(final FormData formData, final OutputStream output) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(output);
        writer.write(this.boundary);
        writer.write("\r\n");
        switch (formData.getType()) {
        case VARIABLE:
            writer.write("Content-Disposition: form-data; name=\"" + formData.getName() + "\"");
            writer.write("\r\n\r\n");
            writer.write(formData.getValue() + "\r\n");
            break;
        case DATA:
            writer.write("Content-Disposition: form-data; name=\"" + formData.getName() + "\"; filename=\"" + formData.getValue() + "\"");
            writer.write("\r\nContent-Type: " + formData.getDataType() + "\r\n\r\n");
            writer.flush();
            output.write(formData.getData(), 0, formData.getData().length);
            output.flush();
            writer.write("\r\n");
            writer.flush();
            break;
        case FILE:
            writer.write("Content-Disposition: form-data; name=\"" + formData.getName() + "\"; filename=\"" + formData.getValue() + "\"");
            writer.write("\r\nContent-Type: " + formData.getDataType() + "\r\n\r\n");
            writer.flush();
            final byte[] b = new byte[1024];
            InputStream in = null;
            try {
                in = new FileInputStream(formData.getFile());
                int n;
                while ((n = in.read(b)) > -1) {
                    output.write(b, 0, n);
                }
                output.flush();
                writer.write("\r\n");
                writer.flush();
            } finally {
                if (in != null) {
                    in.close();
                }
            }
            break;
        }
        writer.flush();
        output.flush();
    }

    private void write(final FormData formData, final StringBuffer sb) {
        sb.append(this.boundary);
        sb.append("\r\n");
        switch (formData.getType()) {
        case VARIABLE:
            sb.append("Content-Disposition: form-data; name=\"").append(formData.getName()).append("\"");
            sb.append("\r\n\r\n");
            sb.append(formData.getValue()).append("\r\n");
            break;
        case DATA:
            sb.append("Content-Disposition: form-data; name=\"").append(formData.getName()).append("\"; filename=\"").append(formData.getValue()).append("\"");
            sb.append("\r\nContent-Type: ").append(formData.getDataType());
            sb.append("\r\n\r\n[.....").append(formData.getData().length).append(" Byte DATA....]\r\n");
            break;
        case FILE:
            sb.append("Content-Disposition: form-data; name=\"").append(formData.getName()).append("\"; filename=\"").append(formData.getValue()).append("\"");
            sb.append("\r\nContent-Type: ").append(formData.getDataType());
            sb.append("\r\n\r\n[.....").append(formData.getFile().length()).append(" FileByte DATA....]\r\n");
            break;
        }
    }

}
