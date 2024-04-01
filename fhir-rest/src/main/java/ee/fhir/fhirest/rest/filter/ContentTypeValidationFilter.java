package ee.fhir.fhirest.rest.filter;

import ee.fhir.fhirest.core.exception.FhirException;
import ee.fhir.fhirest.rest.model.FhirestRequest;
import ee.fhir.fhirest.structure.service.ContentTypeService;
import java.util.List;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ContentTypeValidationFilter implements FhirestRequestFilter {
  private final List<MediaType> supportedMediaTypes;

  public ContentTypeValidationFilter(ContentTypeService contentTypeService) {
    this.supportedMediaTypes = MediaType.parseMediaTypes(contentTypeService.getMediaTypes());
  }

  @Override
  public Integer getOrder() {
    return VALIDATE;
  }

  @Override
  public void handleRequest(FhirestRequest req) {
    if (!req.getAccept().isEmpty() &&
        req.getAccept().stream().noneMatch(a -> a.equalsTypeAndSubtype(MediaType.ALL) || a.isPresentIn(supportedMediaTypes))) {
      throw new FhirException(406, IssueType.NOTSUPPORTED, "invalid Accept");
    }
    if (req.getContentType() != null && !req.getContentType().isPresentIn(supportedMediaTypes)) {
      throw new FhirException(406, IssueType.NOTSUPPORTED, "invalid Content-Type");
    }
  }

}
