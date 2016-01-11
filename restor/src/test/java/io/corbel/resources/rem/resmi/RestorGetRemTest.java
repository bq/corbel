package io.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.resources.rem.model.RestorResourceUri;
import io.corbel.resources.rem.model.RestorObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.restor.RestorGetRem;

/**
 * @author Alberto J. Rubio
 */
public class RestorGetRemTest {

	private RestorDao daoMock;
	private RequestParameters requestParameters;
	private InputStream entity;

	private RestorGetRem getRem;

	@Before
	public void setup() {
		daoMock = Mockito.mock(RestorDao.class);
		requestParameters = Mockito.mock(RequestParameters.class);
		entity = Mockito.mock(InputStream.class);
		getRem = new RestorGetRem(daoMock);
	}

	@Test
	public void testGetOkResource() {
		when(requestParameters.getAcceptedMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_XML));
        when(requestParameters.getRequestDomain()).thenReturn("RequestedDomain");
		when(daoMock.getObject(new RestorResourceUri("RequestedDomain", MediaType.APPLICATION_XML.toString(), "test", "resourceId"))).thenReturn(
				new RestorObject(MediaType.APPLICATION_XML.toString(), entity, null));
		Response response = getRem.resource("test", new ResourceId("resourceId"), requestParameters, Optional.empty());
		String responseContentType = response.getMetadata().getFirst("Content-Type").toString();
		assertThat(responseContentType).isEqualTo(MediaType.APPLICATION_XML_VALUE);
		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	public void testGetNotExistsResource() {
		when(requestParameters.getAcceptedMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_XML));
		Response response = getRem.resource("test", new ResourceId("notExistsId"), requestParameters, Optional.empty());
		assertThat(response.getStatus()).isEqualTo(404);
	}

}
