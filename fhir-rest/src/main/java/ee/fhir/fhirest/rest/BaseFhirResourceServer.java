/*
 * MIT License
 *
 * Copyright (c) 2024 FHIRest community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.fhir.fhirest.rest;

import ee.fhir.fhirest.core.exception.FhirException;
import ee.fhir.fhirest.core.exception.FhirestIssue;
import ee.fhir.fhirest.core.model.ResourceVersion;
import ee.fhir.fhirest.core.util.DateUtil;
import ee.fhir.fhirest.rest.model.FhirestRequest;
import ee.fhir.fhirest.rest.model.FhirestResponse;
import ee.fhir.fhirest.rest.util.PreferredReturn;
import ee.fhir.fhirest.structure.api.ResourceContent;
import ee.fhir.fhirest.structure.service.ResourceFormatService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.LinkRelationTypes;
import org.hl7.fhir.r5.model.OperationOutcome;

public abstract class BaseFhirResourceServer implements FhirResourceServer {
  @Inject
  private HttpServletRequest request;
  @Inject
  private ServerUriHelper serverUriHelper;
  @Override
  public abstract String getTargetType();

  @Override
  public FhirestResponse read(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse vread(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse create(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse update(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse conditionalUpdate(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse delete(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse history(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse historyType(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse search(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse search_(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse instanceOperation(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse instanceOperation_(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse typeOperation(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }

  @Override
  public FhirestResponse typeOperation_(FhirestRequest req) {
    throw new FhirException(FhirestIssue.FEST_001, "desc", "interaction not supported");
  }


  protected FhirestResponse created(ResourceVersion version, FhirestRequest req) {
    return new FhirestResponse(201, preferedBody(version, req))
        .header("Location", uri(version, req))
        .header("ETag", version.getETag())
        .header("Last-Modified", DateUtil.format(version.getModified(), DateUtil.ISO_DATETIME));
  }

  protected FhirestResponse updated(ResourceVersion version, FhirestRequest req) {
    return new FhirestResponse(200, preferedBody(version, req))
        .header("Content-Location", uri(version, req))
        .header("ETag", version.getETag())
        .header("Last-Modified", DateUtil.format(version.getModified(), DateUtil.ISO_DATETIME));
  }

  protected ResourceContent preferedBody(ResourceVersion version, FhirestRequest req) {
    String prefer = PreferredReturn.parse(req.getHeaders());
    if (StringUtils.equals(prefer, PreferredReturn.OperationOutcome)) {
      return ResourceFormatService.get().compose(new OperationOutcome(), "json");
    }
    if (StringUtils.equals(prefer, PreferredReturn.representation)) {
      return version.getContent();
    }
    return null;
  }

  protected String uri(ResourceVersion version, FhirestRequest req) {
    return serverUriHelper.getServerUri() + "/" + RuleThemAllFhirController.FHIR_ROOT + "/" + version.getReference();
  }

  protected void addPagingLinks(Bundle bundle, Integer count, Integer page, FhirestRequest req) {
    if (count == 0) {
      return;
    }
    String pageUrl = serverUriHelper.getServerHost() + request.getRequestURI();
    String queryString = request.getQueryString(); //TODO: migronaut
    queryString = StringUtils.isEmpty(queryString) ? "" : RegExUtils.removePattern(queryString, "[&?]?_page=[0-9]+");
    pageUrl += StringUtils.isEmpty(queryString) ? "?_page=" : ("?" + queryString + "&_page=");

    bundle.addLink().setRelation(LinkRelationTypes.SELF).setUrl(pageUrl + page);
    bundle.addLink().setRelation(LinkRelationTypes.FIRST).setUrl(pageUrl + 1);
    bundle.addLink().setRelation(LinkRelationTypes.LAST).setUrl(pageUrl + Math.max(1, (int) Math.ceil((double) bundle.getTotal() / count)));
    if (page > 1) {
      bundle.addLink().setRelation(LinkRelationTypes.PREVIOUS).setUrl(pageUrl + (page - 1));
    }
    if (page * count < bundle.getTotal()) {
      bundle.addLink().setRelation(LinkRelationTypes.NEXT).setUrl(pageUrl + (page + 1));
    }
  }
}

