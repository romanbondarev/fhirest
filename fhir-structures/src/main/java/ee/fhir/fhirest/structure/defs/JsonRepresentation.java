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

package ee.fhir.fhirest.structure.defs;

import ee.fhir.fhirest.structure.api.ParseException;
import ee.fhir.fhirest.structure.api.ResourceRepresentation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.formats.IParser;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.formats.JsonParser;
import org.hl7.fhir.r5.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class JsonRepresentation implements ResourceRepresentation {

  @Override
  public List<String> getMimeTypes() {
    return Arrays.asList("application/fhir+json", "application/json+fhir", "application/json", "text/json", "json");
  }

  @Override
  public String getName() {
    return "json";
  }

  @Override
  public String compose(Resource resource) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      JsonParser parser = new JsonParser();
      parser.compose(output, resource);
      return output.toString(StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isParsable(String input) {
    String strip = StringUtils.stripStart(input, null);
    return StringUtils.startsWithAny(strip, "{", "[");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> R parse(String input) {
    try {
      return (R) new JsonParser().parse(input);
    } catch (Exception e) {
      throw new ParseException(e);
    }
  }

  @Override
  public String prettify(String content) {
    //TODO: ugly
    IParser p = new JsonParser().setOutputStyle(OutputStyle.PRETTY);
    try {
      return p.composeString(p.parse(content));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
