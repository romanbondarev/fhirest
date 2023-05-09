package com.kodality.kefhir.rest.model;

import com.kodality.kefhir.core.model.VersionId;
import com.kodality.kefhir.core.util.ResourceUtil;
import com.kodality.kefhir.rest.KefhirEndpointService.KefhirEnabledOperation;
import io.micronaut.http.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class KefhirRequest {
  private String method;
  private String type;
  private String path;
  private Map<String, List<String>> parameters = new LinkedHashMap<>();
  private Map<String, List<String>> headers = new LinkedHashMap<>();
  private String uri;
  private String serverUri;
  private String serverHost;
  private String body;
  private List<MediaType> accept;
  private MediaType contentType;

  private String transactionMethod;
  private KefhirEnabledOperation operation;

  public void setAccept(MediaType accept) {
    this.accept = List.of(accept);
  }

  public void setAccept(List<MediaType> accept) {
    this.accept = accept;
  }

  public String getContentTypeName() {
    return contentType == null ? null : contentType.getName();
  }

  public void setPath(String path) {
    this.path = StringUtils.removeEnd(StringUtils.removeStart(path, "/"), "/");
  }

  public VersionId getReference() {
    return ResourceUtil.parseReference(type + "/" + getPath());
  }

  public String getHeader(String name) {
    return headers.containsKey(name) && !headers.get(name).isEmpty() ? headers.get(name).get(0) : null;
  }

  public void putHeader(String name, String value) {
    if (name == null || value == null) {
      return;
    }
    if (name.equalsIgnoreCase("Accept")) {
      setAccept(List.of(MediaType.of(value.split(","))));
      return;
    }
    if (name.equalsIgnoreCase("Content-Type")) {
      setContentType(MediaType.of(value));
      return;
    }
    headers.computeIfAbsent(name, x -> new ArrayList<>()).add(value);
  }

  public String getParameter(String name) {
    return parameters.containsKey(name) && !parameters.get(name).isEmpty() ? parameters.get(name).get(0) : null;
  }

  public void putParameter(String name, String value) {
    if (name != null && value != null) {
      parameters.computeIfAbsent(name, x -> new ArrayList<>()).add(value);
    }
  }

  public void putQuery(String query) {
    if (query == null) {
      return;
    }
    Arrays.stream(query.split("&")).forEach(q -> {
      String[] p = q.split("=");
      putParameter(p[0], p[1]);
    });
  }
}
