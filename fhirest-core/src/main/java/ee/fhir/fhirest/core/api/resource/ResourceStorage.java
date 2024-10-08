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

package ee.fhir.fhirest.core.api.resource;

import ee.fhir.fhirest.core.model.ResourceId;
import ee.fhir.fhirest.core.model.ResourceVersion;
import ee.fhir.fhirest.core.model.VersionId;
import ee.fhir.fhirest.core.model.search.HistorySearchCriterion;
import ee.fhir.fhirest.structure.api.ResourceContent;
import java.util.List;

/**
 * Interface for implementing a FHIR resource storage.
 * There can be only on implementation for every resource type.
 */
public interface ResourceStorage {
  String DEFAULT = "default";

  /**
   * <p>A FHIR resource type this implementation will only be applied for.</p>
   * <p>Use value "<b>default</b>" to apply this to all other resources</p>
   */
  default String getResourceType() {
    return DEFAULT;
  }

  ResourceVersion save(ResourceId id, ResourceContent content);

  void delete(ResourceId id);

  ResourceVersion load(VersionId id);

  List<ResourceVersion> load(List<VersionId> ids);

  List<ResourceVersion> loadHistory(HistorySearchCriterion criteria);

  /**
   * @return Unique resource id from sequence to be saved later
   */
  String generateNewId();
}
