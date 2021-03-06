import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

import feign.FeignException;
import ru.hh.nab.starter.NabApplication;
import ru.hh.nab.starter.jersey.ObjectMapperContextResolver;
import ru.hh.nab.testbase.NabTestBase;
import ru.hh.school.dto.EmployerDto;
import ru.hh.school.dto.VacancyDto;
import ru.hh.school.exceptionmapper.FeignExceptionMapper;
import ru.hh.school.service.VacancyService;

@ContextConfiguration(classes = VacancyResourceTest.Config.class)
public class VacancyResourceTest extends NabTestBase {

  @Inject
  private VacancyService service;

  @Override
  protected NabApplication getApplication() {
    return NabApplication.builder().configureJersey()
      .registerResources(FeignExceptionMapper.class, ObjectMapperContextResolver.class)
      .bindToRoot().build();
  }

  private static VacancyDto getEmptyVacancy() {
    VacancyDto vacancy = new VacancyDto();
    vacancy.setEmployer(new EmployerDto());
    return vacancy;
  }

  @Test
  public void getVacanciesShouldReturnOk() throws IOException {
    when(service.getVacancies("empty", null, null))
      .thenReturn(List.of(getEmptyVacancy()));
    Response response = createRequest("/vacancy?query=empty").get();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void getVacancyShouldReturnOk() throws JsonProcessingException {
    when(service.getVacancy(0))
      .thenReturn(getEmptyVacancy());
    Response response = createRequest("/vacancy/0").get();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void getVacanciesShouldDelegateBadRequest() throws IOException {
    when(service.getVacancies(null, -1, null))
      .thenThrow(new FeignException(400, "Bad request") {});
    Response response = createRequest("/vacancy?page=-1").get();
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void getVacancyShouldDelegateNotFound() throws JsonProcessingException {
    when(service.getVacancy(-1))
      .thenThrow(new FeignException(404, "Not found") {});
    Response response = createRequest("/vacancy/-1").get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Configuration
  @Import(AppTestConfig.class)
  public static class Config {

    @Bean
    @Primary
    public VacancyService getVacancyService() {
      return Mockito.mock(VacancyService.class);
    }
  }
}
