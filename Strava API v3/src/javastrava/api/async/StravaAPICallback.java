package javastrava.api.async;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Dan Shannon
 * @param <T>
 *            The type being returned to the callback
 *
 */
public class StravaAPICallback<T> implements Callback<T> {
	/**
	 * A Future which will be completed when the call to the API is complete
	 */
	private final StravaAPIFuture<T> future;

	/**
	 * @param completableFuture
	 *            A Future which will be completed when the call to the API is complete
	 */
	public StravaAPICallback(final StravaAPIFuture<T> completableFuture) {
		this.future = completableFuture;
	}

	public void onResponse(Call<T> t, Response<T> response) {
		this.future.complete(response.body());
	}

	public void onFailure(Call<T> t, Throwable error) {
		this.future.completeExceptionally(error);
	}
}
