package javastrava.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javastrava.config.Messages;
import javastrava.json.JsonUtil;
import javastrava.json.exception.JsonSerialisationException;
import javastrava.json.impl.JsonUtilImpl;
import javastrava.model.StravaAPIError;
import javastrava.model.StravaResponse;
import javastrava.service.exception.BadRequestException;
import javastrava.service.exception.InvalidTokenException;
import javastrava.service.exception.NotFoundException;
import javastrava.service.exception.StravaAPINetworkException;
import javastrava.service.exception.StravaAPIRateLimitException;
import javastrava.service.exception.StravaInternalServerErrorException;
import javastrava.service.exception.StravaServiceUnavailableException;
import javastrava.service.exception.StravaUnknownAPIException;
import javastrava.service.exception.UnauthorizedException;
import retrofit2.Response;

/**
 * <p>
 * Interceptor to handle errors thrown by the Strava API.
 * </p>
 *
 * <p>
 * HTTP error status codes are converted to checked (if recoverable) or unchecked (if not) exceptions and thrown
 * </p>
 *
 * <p>
 * With each error, Strava also returns a set of error messages which are encapsulated in {@link StravaResponse}
 * </p>
 *
 * @author Dan Shannon
 *
 */
public class RetrofitErrorHandler {
	/**
	 * Logger
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * <p>
	 * Determines whether a token is invalid or not
	 * </p>
	 *
	 * @param response
	 *            response received from the Strava API call
	 * @return <code>true</code> if the token is invalid, <code>false</code> otherwise
	 */
	private static boolean tokenInvalid(final StravaResponse response) {
		if ((response == null) || (response.getErrors() == null) || response.getErrors().isEmpty()) {
			return false;
		}
		for (final StravaAPIError error : response.getErrors()) {
			if (error.getResource().equals("Application") && error.getCode().equals("invalid")) { //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
		}
		return false;
	}

	/**
	 * JSON utilities for serialisation and deserialisation
	 */
	public static Throwable handleError(final Response error, Throwable cause) {
	    StravaResponse response = new StravaResponse();
		final String status = (error == null ? Messages.string("RetrofitErrorHandler.unknownError") //$NON-NLS-1$
				: error.code() + " " + error.message()); //$NON-NLS-1$

		// Handle network errors
		if (error.code() == 500) {
			response.setMessage(error.message());

			StravaAPIError apiError = new StravaAPIError();
			apiError.setCode(error.code() + "");
			apiError.setResource(error.errorBody().toString());
			response.addError(apiError);
			return new StravaAPINetworkException(null, response, null);
		}

		if (error == null) {
			return new StravaUnknownAPIException(status, response, cause);
		}

		try {
			if (error.errorBody() == null) {
				response = new StravaResponse();
				response.setMessage(cause.getMessage());
			} else {
                JsonUtil json = new JsonUtilImpl();
                response = json.deserialise(error.errorBody().toString(), StravaResponse.class);
			}
		} catch (final JsonSerialisationException e) {
			response = new StravaResponse();
			response.setMessage(error.errorBody().toString());
		} catch (final Exception e) {
			response = new StravaResponse();
			response.setMessage(cause.toString());
		}

		// Handle 400 Bad request error
		if (error.code() == 400) {
			log.warn(status + " : " + response); //$NON-NLS-1$
			return new BadRequestException(status, response, cause);
		}

		// Handle 401 Unauthorized error
		if (error.code() == 401) {
			if (tokenInvalid(response)) {
				log.info(status + " : " + response); //$NON-NLS-1$
				return new InvalidTokenException(status, response, cause);
			}

			log.warn(status + " : " + response); //$NON-NLS-1$
			return new UnauthorizedException(status, response, cause);
		}

		// Handle 403 forbidden error
		if (error.code() == 403) {
			log.info(status + " : " + response); //$NON-NLS-1$
			if (response.getMessage().equals(Messages.string("RetrofitErrorHandler.rateLimitExceeded"))) { //$NON-NLS-1$
				return new StravaAPIRateLimitException(status, response, cause);
			}
			return new UnauthorizedException(status, response, cause);
		}

		// Handle 404 Not Found error
		if (error.code() == 404) {
			log.info(status + " : " + response); //$NON-NLS-1$
			return new NotFoundException(response, cause);
		}

		// Handle 429 Too many requests error
		if (error.code() == 429) {
			log.warn(status + " : " + response); //$NON-NLS-1$
			return new StravaAPIRateLimitException(status, response, cause);
		}

		// Handle 500 Internal Server error
		if (error.code() == 500) {
			log.error(status + " : " + response); //$NON-NLS-1$
			return new StravaInternalServerErrorException(status, response, cause);
		}

		// Handle 503 Service Unavailable error
		if (error.code() == 503) {
			log.error(status + " : " + response); //$NON-NLS-1$
			return new StravaServiceUnavailableException(status, response, cause);
		}

		log.error(response);
		return new StravaUnknownAPIException(status, response, cause);
	}
}
