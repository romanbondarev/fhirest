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
import org.apache.commons.lang3.StringUtils;

public class QuantityExpressionProvider extends NumberExpressionProvider {

  @Override
  protected SqlBuilder makeCondition(QueryParam param, String value) {
    if (!value.contains("|")) {
      return super.makeCondition(param, value);
    }
    String[] parts = value.split("\\|");
    if (parts.length != 3 || StringUtils.isEmpty(parts[2])) {
      throw new FhirException(FhirestIssue.FEST_032, "value", value);
    }
    SqlBuilder sb = super.makeCondition(param, parts[0]);
    if (StringUtils.isEmpty(parts[1])) {
      sb.append(" and (i.code = ? or i.unit = ?)", parts[2], parts[2]);
    } else {
      sb.append(" and i.system_id = search.sys_id(?) and i.code = ?", parts[1], parts[2]);
    }
    return sb;
  }

}
