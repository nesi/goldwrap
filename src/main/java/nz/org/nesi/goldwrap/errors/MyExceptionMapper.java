package nz.org.nesi.goldwrap.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<ServiceException> {

	public Response toResponse(ServiceException ex) {
		return Response.status(ex.getFaultInfo().getErrorCode())
				.entity(ex.getFaultInfo()).build();
	}

}
