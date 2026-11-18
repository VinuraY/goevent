import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { API_BASE_URL, API_PREFIX } from '@/api/routes';
import { loading } from '@/lib/loading-store';

/** Keeps the loading screen up briefly after the last response so it does not flicker. */
const LOADING_FADE_MS = 300;

const CSRF_HEADER = 'X-CSRF-TOKEN';
const SAFE_METHODS = new Set(['get', 'head', 'options']);

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

/**
 * Fetches a fresh token for each write. Login and logout rotate the session,
 * so a token from an earlier request must not be reused.
 */
async function fetchCsrfToken(): Promise<string> {
  const { data } = await apiClient.get<{ headerName?: string; token?: string }>(`${API_PREFIX}/auth/csrf`);
  if (data.headerName !== CSRF_HEADER || typeof data.token !== 'string' || !data.token) {
    throw new Error('Unable to verify the security token. Refresh before retrying.');
  }
  return data.token;
}

// Every request shows the loading screen until it settles. The store counts
// in-flight requests, so the screen stays up while any of them is still running.
// Writes also carry a CSRF token unless the caller already set one.
apiClient.interceptors.request.use(
  async (config) => {
    loading.open();
    const method = (config.method ?? 'get').toLowerCase();
    if (!SAFE_METHODS.has(method) && !config.headers.has(CSRF_HEADER)) {
      config.headers.set(CSRF_HEADER, await fetchCsrfToken());
    }
    return config;
  },
  (error) => {
    loading.close(LOADING_FADE_MS);
    return Promise.reject(error);
  }
);

// Normalise failures so call sites receive a predictable `{ message }` shape.
// Toasts are still managed explicitly by each request handler.
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    loading.close(LOADING_FADE_MS);
    return response;
  },
  (error) => {
    loading.close(LOADING_FADE_MS);
    const customError = {
      message: error.response?.data?.message || error.message || 'An unexpected error occurred',
      errorCode: error.response?.data?.errorCode || 'NETWORK_ERROR',
      status: error.response?.status,
    };
    return Promise.reject(customError);
  }
);
