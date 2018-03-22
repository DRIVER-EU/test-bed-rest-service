package eu.driver.adaptor.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.avro.generic.GenericRecord;
import org.apache.log4j.Logger;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adaptor.callback.AdapterCallback;
import eu.driver.adaptor.ws.CallbackController;

@RestController
public class ResponseController implements ResourceProcessor<RepositoryLinksResource> {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SendRestController.class).sendXMLMessage("CAP", "defaultCGOR", "XML")).withRel("sendXMLMessage"));
	    return resource;
	}
	
	public ResponseController() {
		
	}
	
	@ApiOperation(value = "addRESTEndpoint", nickname = "addRESTEndpoint")
	@RequestMapping(value = "/CISRestAdaptor/addRESTEndpoint", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "url", value = "The path of the Endpoint", required = true, dataType = "string", paramType = "query")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Response.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Response.class),
            @ApiResponse(code = 500, message = "Failure", response = Response.class)})
	@Produces({"application/json"})
	public ResponseEntity<Response> addRESTEndpoint(	@QueryParam("url") String url ) {
		log.info("--> addRESTEndpoint");
		log.debug(url);
		
		Response response = new Response();
		
		CallbackController.getInstance().setRestEndpointUrl(url);
		
		response.setMessage("REST Endpoint is configured");
		log.info("addRESTEndpoint -->");
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
}
