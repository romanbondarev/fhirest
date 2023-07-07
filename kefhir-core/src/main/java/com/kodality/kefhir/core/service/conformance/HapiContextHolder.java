package com.kodality.kefhir.core.service.conformance;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import com.kodality.kefhir.core.api.conformance.ConformanceUpdateListener;
import com.kodality.kefhir.core.api.conformance.HapiValidationSupportProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r5.model.Enumerations.CodeSystemContentMode;
import org.hl7.fhir.r5.model.PackageInformation;
import org.hl7.fhir.r5.model.Resource;

@RequiredArgsConstructor
@Singleton
public class HapiContextHolder implements ConformanceUpdateListener {

  public static final List<CodeSystemContentMode> SUPPORTED_CS_CONTENT_MODES = Arrays.asList(CodeSystemContentMode.COMPLETE, CodeSystemContentMode.FRAGMENT);

  protected IWorkerContext hapiContext;
  protected FhirContext context;
  protected FhirValidator validator;
  protected final List<HapiValidationSupportProvider> validationSupportProviders;

  public IWorkerContext getHapiContext() {
    return hapiContext;
  }

  public FhirContext getContext() {
    return context;
  }

  public FhirValidator getValidator() {
    return validator;
  }

  @PostConstruct
  public void init() {
    context = FhirContext.forR5();
  }

  @Override
  public void updated() {
    IValidationSupport validationSupport = getValidationSupport();

    hapiContext = new HapiWorkerContext(context, validationSupport);
    context.setValidationSupport(validationSupport);

    validator = context.newValidator();
    validator.registerValidatorModule(new FhirInstanceValidator(validationSupport));
    preloadHapi();
  }

  protected IValidationSupport getValidationSupport() {
    Map<String, IBaseResource> defs = ConformanceHolder.getDefinitions().stream().collect(Collectors.toMap(d -> d.getUrl(), d -> d));
    // hack to bypass hapi structuredefinition.type validation, (#see org.hl7.fhir.validation.instance.InstanceValidator.checkTypeValue)
    // because wo don't have information about "source package" and it isn't even a part of fhir resource.
    // go to hapi hell.
    defs.values().forEach(def -> ((Resource) def).setSourcePackage(new PackageInformation("hl7.fhir.r", def.getMeta().getLastUpdated())));
    Map<String, IBaseResource> vs = ConformanceHolder.getValueSets().stream().collect(Collectors.toMap(d -> d.getUrl(), d -> d));
    Map<String, IBaseResource> cs = ConformanceHolder.getCodeSystems().stream()
        .filter(c -> SUPPORTED_CS_CONTENT_MODES.contains(c.getContent()))
        .collect(Collectors.toMap(d -> d.getUrl(), d -> d));

    ValidationSupportChain chain = new ValidationSupportChain(
        new InMemoryTerminologyServerValidationSupport(context),
        new PrePopulatedValidationSupport(context, defs, vs, cs),
        new CommonCodeSystemsTerminologyService(context),
        new SnapshotGeneratingValidationSupport(context)
    );
    validationSupportProviders.forEach(p -> chain.addValidationSupport(p.getValidationSupport(context)));
    return new CachingValidationSupport(chain);
  }

  private void preloadHapi() {
    try {
      validator.validateWithResult("{}");
    } catch (Exception e) {
      //ignore
    }
  }

}
