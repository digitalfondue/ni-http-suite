/**
 * Copyright © 2020 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.nihttpsuite;

import javax.json.JsonValue;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Http response. Set the response code and a body.
 */
public interface Response {

    /**
     * Set http status code.
     *
     * @param code status code
     * @return response
     */
    Response code(int code);

    /**
     * Set http status code 200.
     *
     * @return response
     */
    default Response ok() {
        return code(200);
    }

    /**
     * Set http status code 404.
     *
     * @return response
     */
    default Response notFound() {
        return code(404);
    }

    /**
     * Set header. If there is already another one with the same name, it will be overwritten.
     *
     * @param name  header name
     * @param value header value
     * @return response
     */
    Response header(String name, String value);

    /**
     * Add header. If there is already another one with the same name, it will be added.
     *
     * @param name  header name
     * @param value header value
     * @return response
     */
    Response addHeader(String name, String value);

    default Response body(String body, String contentType) {
        return body(body, contentType, StandardCharsets.UTF_8);
    }

    default Response html(String body) {
        return body(body, "text/html");
    }

    default Response json(JsonValue value) {
        return body(value.toString(), "application/json");
    }

    Response body(String body, String contentType, Charset charset);

    //
    default Response body(File file) {
        return body(file, "application/octet-stream");
    }

    default Response body(File file, String contentType) {
        return body(file, contentType, null);
    }

    Response body(File file, String contentType, Charset charset);
    //


    //
    default Response body(Path path) {
        return body(path, "application/octet-stream");
    }

    default Response body(Path path, String contentType) {
        return body(path, contentType, null);
    }

    Response body(Path path, String contentType, Charset charset);
    //

    //
    default Response body(InputStream inputStream) {
        return body(inputStream, "application/octet-stream");
    }

    default Response body(InputStream inputStream, String contentType) {
        return body(inputStream, contentType, null);
    }

    Response body(InputStream inputStream, String contentType, Charset charset);
    //


    //
    default Response body(byte[] body) {
        return body(body, "application/octet-stream");
    }

    default Response body(byte[] body, String contentType) {
        return body(body, contentType, null);
    }

    Response body(byte[] body, String contentType, Charset charset);
    //

    //
    default Response body(ByteBuffer body) {
        return body(body, "application/octet-stream");
    }

    default Response body(ByteBuffer body, String contentType) {
        return body(body, contentType, null);
    }

    Response body(ByteBuffer body, String contentType, Charset charset);
    //
}

