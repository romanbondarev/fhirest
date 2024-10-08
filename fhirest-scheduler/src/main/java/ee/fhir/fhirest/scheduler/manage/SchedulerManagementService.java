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

package ee.fhir.fhirest.scheduler.manage;

import ee.fhir.fhirest.scheduler.SchedulerJob;
import ee.fhir.fhirest.scheduler.SchedulerJobQueryParams;
import ee.fhir.fhirest.scheduler.SchedulerJobRepository;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SchedulerManagementService {
  private final SchedulerJobRepository jobRepository;

  public List<SchedulerJob> findJobs(SchedulerJobQueryParams params) {
    return jobRepository.query(params);
  }

  public void rerun(Long id) {
    List<SchedulerJob> result = jobRepository.query(new SchedulerJobQueryParams().setStatus("failed").setId(id));
    if (result.size() != 1) {
      throw new SchedulerJobException("Could not find unique failed job by id " + id);
    }
    SchedulerJob job = result.get(0);
    jobRepository.markRerun(id);
    jobRepository.insert(job.getType(), job.getIdentifier(), new Date());
  }

}
