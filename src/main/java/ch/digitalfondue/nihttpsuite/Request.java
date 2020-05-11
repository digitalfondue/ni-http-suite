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

import java.util.List;
import java.util.Optional;

/**
 * Represent a HTTP request.
 */
public interface Request {

    /**
     * Return requested path. For example: "/test/path"
     *
     * @return path.
     */
    String getPath();

    /**
     * Get list of header names.
     *
     * @return
     */
    List<String> getHeaderNames();

    /**
     * Get list of headers with the given name.
     *
     * @param name
     * @return
     */
    List<String> getHeaders(String name);

    /**
     * Return first header value for a given name.
     *
     * @param name
     * @return
     */
    Optional<String> getHeader(String name);

    /**
     * Get first value for given query parameter name.
     *
     * @param name
     * @return
     */
    Optional<String> getQueryParameter(String name);

    /**
     * Get all values for given query parameter name.
     *
     * @param name
     * @return
     */
    List<String> getQueryParameters(String name);

    /**
     * Get context. See {@link Context}
     *
     * @return
     */
    Context context();

    /**
     * Get value of path variable defined in route.
     * For example: "/my/{var}" -> path is "/my/test" -> getPathVariable("var") return "test".
     *
     * @param name
     * @return
     */
    String getPathVariable(String name);
}

