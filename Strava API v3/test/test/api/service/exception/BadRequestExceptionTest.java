package test.api.service.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import stravajava.api.v3.model.StravaResponse;
import stravajava.api.v3.service.exception.BadRequestException;

public class BadRequestExceptionTest {

	
	/**
	 * Test constructor
	 */
	@Test
	public void testConstructor_normal() {
		BadRequestException e = new BadRequestException("Test",new StravaResponse(),new IllegalArgumentException());
		try {
			throw e;
		} catch (BadRequestException ex) {
			// Expected
			return;
		}
	}
	
	@Test
	public void testConstructor_nullSafety() {
		new BadRequestException(null, null, null);
	}
	
	@Test
	public void testGetSetResponse() {
		StravaResponse response = new StravaResponse();
		response.setMessage("Test");
		BadRequestException e = new BadRequestException("Test",new StravaResponse(),null);
		e.setResponse(response);
		StravaResponse testResponse = e.getResponse();
		assertEquals(response,testResponse);
	}
}