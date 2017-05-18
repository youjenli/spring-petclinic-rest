package org.springframework.samples.petclinic.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.service.DemoService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("api/demo")
public class DemoController {

	Logger logger = LoggerFactory.getLogger(DemoController.class);
	@Autowired
	private DemoService demoService;
	private RestTemplate rest;
	
	public DemoController(){
		this.rest = new RestTemplate();
	}
	
	@RequestMapping(value = "/clientError/{code}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> clientMistake(@PathVariable("code") int code){
		ResponseEntity<String> entity;
		switch(code) {		
			case 200:
				entity = new ResponseEntity<>(HttpStatus.OK);
				break;		
			case 400:
				entity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				break;
			case 401:
				entity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				break;
			case 403:
				entity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
				break;
			case 404:
			default:
				entity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
				break;
		}		
		logger.info("Generate response code " + entity.getStatusCode() + " at client error service.");
		return entity;
	}
		
	@RequestMapping(value = "/serverFault/{code}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> serverFault(@PathVariable("code") int code){
		ResponseEntity<String> entity;
		switch(code) {
			case 200:
				entity = new ResponseEntity<>(HttpStatus.OK);
				break;
			case 500:
				entity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); 
				break;
			case 501:
				entity = new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
				break;
			case 502:
				entity = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
				break;
			case 503:
			default:
				entity = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
				break;
		}
		logger.info("Generate response code " + entity.getStatusCode() + " at server fault service.");
		return entity;
	}

	@RequestMapping(value = "/serverFault/runtimeException", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Pet> triggerRuntimeException(){
		this.demoService.savePetFailure(null);
		return new ResponseEntity<Pet>(HttpStatus.OK);
	}
	
	//Exception handling approach in spring mvc
	/*
	 * Controller based exception handling using @ExceptionHandler
	 * */
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="data access exception")
	@ExceptionHandler(DataAccessException.class)
	public void handleException(){
		logger.error("DataAccessException has been generated.");
	}
	
	@RequestMapping(value = "/selfInvocation/pet", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Pet[]> getPets(
			@RequestParam(name = "host", required = true) String host,
			@RequestParam(name = "port", required = true) int port){

		ResponseEntity<Pet[]> entity;
		try {
			URL url = new URL("http", host, port, "/petclinic/api/pets");
			entity = (ResponseEntity<Pet[]>)rest.getForEntity(url.toURI(), Pet[].class);	
		} catch(Exception e){
			HttpHeaders headers = new HttpHeaders();
			headers.add("errors", e.toString());
			return new ResponseEntity<Pet[]>(headers, HttpStatus.BAD_REQUEST);
		}
		return entity;
	}
/*	
	@RequestMapping(value = "/selfInvocation/pet", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Pet> addPet(@RequestBody @Valid Pet pet, BindingResult bindingResult,
			@RequestParam(name = "host", required = true) String host,
			@RequestParam(name = "port", required = true) int port){
		BindingErrorsResponse errors = new BindingErrorsResponse();
//		HttpHeaders headers = new HttpHeaders();
		if(bindingResult.hasErrors() || (pet == null)){
			errors.addAllErrors(bindingResult);
			HttpHeaders headers = new HttpHeaders();
			headers.add("errors", errors.toJSON());
			return new ResponseEntity<Pet>(headers, HttpStatus.BAD_REQUEST);
		}
//		this.clinicService.savePet(pet);
		try {
			URL url = new URL("http", host, port, "/petclinic/api/pets");
			return rest.postForEntity(url.toURI(), pet, Pet.class);
		} catch(Exception e){
			HttpHeaders headers = new HttpHeaders();
			headers.add("errors", e.getMessage());
			return new ResponseEntity<Pet>(HttpStatus.BAD_REQUEST);
		}
	}
*/	
}
