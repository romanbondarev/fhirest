/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.fhir.fhirest.search.sql.params;

import ee.fhir.fhirest.core.exception.FhirException;
import ee.fhir.fhirest.core.exception.FhirestIssue;
import ee.fhir.fhirest.core.model.search.QueryParam;
import ee.fhir.fhirest.util.sql.SqlBuilder;

public class UriExpressionProvider extends DefaultExpressionProvider {

  @Override
  protected SqlBuilder makeCondition(QueryParam param, String value) {
    if (param.getModifier() == null) {
      return new SqlBuilder("i.uri = ?", value);
    }
    if (param.getModifier().equals("below")) {
      return new SqlBuilder("i.uri like ?", value + "%");
    }
    if (param.getModifier().equals("above")) {
      return new SqlBuilder("? like (i.uri || '%')", value); //TODO: probably indexes will not work here
    }

    throw new FhirException(FhirestIssue.FEST_001, "desc", "modifier " + param.getModifier());
  }

  @Override
  protected String getOrderField() {
    return "uri";
  }
}
