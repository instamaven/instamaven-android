package com.instamaven.app.utils;

import android.content.Context;
import android.content.Intent;

import com.instamaven.app.R;
import com.instamaven.app.activities.SignInActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * RequestClient usage:
 * <p>
 * String response =  new RequestClient.Builder(context)
 * .addHeader("Accept", "application/json")
 * .setUrl("/api/v1/badges/1")
 * .setMethod("PATCH", "GET", "POST")
 * .addField("title", title)
 * .addField("description", description)
 * .addField("price", price)
 * .addField("latitude", latitude)
 * .addField("message", message)
 * .addField("longitude", longitude)
 * .addField("address", address)
 * .addField("url", webSite)
 * .addField("categories", categoriesAdapter.getSelected().toString())
 * .addFile("image", selectedFile)
 * .send();
 */

public class RequestClient {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset = "UTF-8";
    private OutputStream outputStream;
    private PrintWriter writer;
    private boolean isWriterSet = false;
    private HashMap<String, String> headers;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param requestURL Form URL
     * @throws IOException
     */

    protected RequestClient(String requestURL, String method) throws IOException {

        headers = new HashMap<>();

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoInput(true);
        if (!method.equalsIgnoreCase("GET")) {
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
        } else {
            httpConn.setDoOutput(false);
        }
    }

    private void setWriter() throws IOException {
        if (!isWriterSet) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpConn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
        isWriterSet = true;
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */

    protected void addFormField(String name, String value) throws IOException {
        if (!isWriterSet) {
            setWriter();
        }
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload selectedFile section to the request
     *
     * @param fieldName    name attribute in <input type="selectedFile" name="..." />
     * @param selectedFile
     * @throws IOException
     */

    protected void addFilePart(String fieldName, SelectedFile selectedFile) throws IOException {
        if (!isWriterSet) {
            setWriter();
        }
        String fileName = selectedFile.getFilename();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(selectedFile.getContent());
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        fileInputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */

    protected void addHeader(String name, String value) throws Exception {
        if (!isWriterSet) {
            headers.put(name, value);
        } else {
            throw new Exception("Add Header should be called before any other fields have been added");
        }
    }

    /**
     * Completes non GET request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */

    protected String submit() throws IOException {
        if (!isWriterSet) {
            setWriter();
        }
        String response = "";

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response = response.concat(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }

    /**
     * Completes GET request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */

    protected String send() throws IOException {
        String response = "";

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response = response.concat(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }

    public static class Builder {

        private Context mContext;
        private boolean mAuth = true;
        private String mUrl;
        private String mMethod = "POST";
        private HashMap<String, String> headers;
        private HashMap<String, String> fields;
        private HashMap<String, SelectedFile> files;

        public Builder(Context context) {
            mContext = context;
            init();
        }

        public Builder(Context context, String url) {
            this(context);
            setUrl(url);
        }

        public Builder(Context context, String url, String method) {
            this(context);
            setUrl(url);
            setMethod(method);
        }

        public Builder(Context context, String url, String method, boolean auth) {
            this(context);
            setUrl(url);
            setMethod(method);
            setAuth(auth);
        }

        protected void init() {
            headers = new HashMap<>();
            fields = new HashMap<>();
            files = new HashMap<>();
        }

        public Builder setMethod(String method) {
            mMethod = method;
            return this;
        }

        public Builder setUrl(String url) {
            mUrl = url;
            // clear fields and files
            fields = new HashMap<>();
            files = new HashMap<>();
            return this;
        }

        public Builder setAuth(boolean auth) {
            mAuth = auth;
            return this;
        }

        public Builder addHeader(String name, String value) {
            if (value != null) {
                headers.put(name, value);
            }
            return this;
        }

        public Builder addField(String name, String value) {
            if (value != null) {
                fields.put(name, value);
            }
            return this;
        }

        public Builder addFile(String fieldName, SelectedFile selectedFile) {
            if (selectedFile != null) {
                files.put(fieldName, selectedFile);
            }
            return this;
        }

        @Override
        public String toString() {
            return headers.toString() + fields.toString() + files.toString();
        }

        public String send() throws Exception {
            if (mUrl.isEmpty()) {
                throw new Exception(mContext.getString(R.string.url_empty));
            }
            if (mAuth) {
                String token = ProfileHelper.getToken(mContext);
                if (token.isEmpty()) {
                    Intent intent = new Intent(mContext, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    throw new Exception(mContext.getString(R.string.session_expired));
                }

                headers.put("Authorization", "Bearer " + token);
            }
            if (!mMethod.equals("GET") && !mMethod.equals("POST")) {
                addField("_method", mMethod);
                setMethod("POST");
            }
            if (mMethod.equals("GET")) {
                // collect fields
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    if (first) {
                        result.append("?");
                        first = false;
                    } else {
                        result.append("&");
                    }
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                RequestClient client = new RequestClient(mUrl + result.toString(), mMethod);
                // send headers
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    client.httpConn.setRequestProperty(entry.getKey(), entry.getValue());
                }

                return client.send();
            } else {
                RequestClient client = new RequestClient(mUrl, mMethod);
                // add headers
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    client.addHeader(entry.getKey(), entry.getValue());
                }
                // add fields
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    client.addFormField(entry.getKey(), entry.getValue());
                }
                // add files
                for (Map.Entry<String, SelectedFile> entry : files.entrySet()) {
                    client.addFilePart(entry.getKey(), entry.getValue());
                }

                return client.submit();
            }
        }
    }
}