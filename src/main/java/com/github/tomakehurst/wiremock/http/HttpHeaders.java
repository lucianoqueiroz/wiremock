/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.http;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

public class HttpHeaders extends HashMap<String, String> {

    private final Multimap<String, String> headers;

    public HttpHeaders() {
        headers = LinkedHashMultimap.create();
    }

    public HttpHeaders(HttpHeader... headers) {
        this();
        for (HttpHeader header: headers) {
            put(header.key(), header.firstValue());
            this.headers.putAll(header.key(), header.values());
        }
    }

    public HttpHeaders(Iterable<HttpHeader> headers) {
        this();
        for (HttpHeader header: headers) {
            put(header.key(), header.firstValue());
            this.headers.putAll(header.key(), header.values());
        }
    }

    public HttpHeaders(HttpHeaders headers) {
        this(headers.all());
    }

    public HttpHeader getHeader(String key) {
        if (!headers.containsKey(key)) {
            return HttpHeader.absent(key);
        }

        Collection<String> values = headers.get(key);
        return new HttpHeader(key, values);
    }

    public boolean hasContentTypeHeader() {
        return headers.containsKey(ContentTypeHeader.KEY);
    }

    public Collection<HttpHeader> all() {
        List<HttpHeader> httpHeaderList = newArrayList();
        for (String key: headers.keySet()) {
            httpHeaderList.add(new HttpHeader(key, headers.get(key)));
        }

        return httpHeaderList;
    }

    @Override
    public String get(Object key) {
        return super.get(key);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String put(String key, String value) {
        headers.put(key, value);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Map.Entry<? extends String, ? extends String> entry: m.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }

        super.putAll(m);
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return super.entrySet();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> keySet() {
        return super.keySet();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static HttpHeaders copyOf(HttpHeaders source) {
        return new HttpHeaders(source);
    }
}
