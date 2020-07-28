package javastrava.api.util;

import java.io.IOException;
import java.util.StringTokenizer;

import javastrava.config.StravaConfig;
import javastrava.service.Strava;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Overrides the OkHttp client in order to intercept the rate limit data returned by the API in headers
 * </p>
 * 
 * @author Dan Shannon
 *
 */
public class RetrofitClientResponseInterceptor implements Interceptor {

	@Override
	public Response intercept(@NotNull Chain chain) throws IOException {

		Response response = chain.proceed(chain.request());
		Headers headers = response.headers();

		for (String name : headers.names()) {
			if (name.equals(StravaConfig.string("strava.rate-limit-usage-header-name"))) { //$NON-NLS-1$
				String values = headers.get(name);
				StringTokenizer tokenizer = new StringTokenizer(values, ","); //$NON-NLS-1$
				Strava.REQUEST_RATE_CURRENT = Integer.valueOf(tokenizer.nextToken()).intValue();
				Strava.REQUEST_RATE_DAILY = Integer.valueOf(tokenizer.nextToken()).intValue();
				Strava.requestRateCurrentPercentage();
			}
			if (name.equals(StravaConfig.string("strava.rate-limit-limit-header-name"))) { //$NON-NLS-1$
				String values = headers.get(name);
				StringTokenizer tokenizer = new StringTokenizer(values, ","); //$NON-NLS-1$
				Strava.RATE_LIMIT_CURRENT = Integer.valueOf(tokenizer.nextToken()).intValue();
				Strava.RATE_LIMIT_DAILY = Integer.valueOf(tokenizer.nextToken()).intValue();
				Strava.requestRateDailyPercentage();
			}
		}

		return response;
	}
}
