import type { BackendErrorResponse, FieldError } from './types'

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: FieldError[]

  constructor(error: BackendErrorResponse) {
    super(error.message)
    this.name = 'ApiError'
    this.status = error.status
    this.code = error.code
    this.fieldErrors = error.fieldErrors ?? []
  }
}

export type ApiClientOptions = {
  baseUrl: string
  getToken?: () => string | null
  onUnauthorized?: () => void
  fetchImpl?: typeof fetch
}

export type ApiClient = ReturnType<typeof createApiClient>

type RequestOptions = {
  method: 'GET' | 'POST' | 'PATCH' | 'DELETE'
  body?: unknown
}

export function createApiClient(options: ApiClientOptions) {
  const baseUrl = options.baseUrl.replace(/\/+$/, '')
  const fetchImpl = options.fetchImpl ?? fetch

  async function request<T>(path: string, requestOptions: RequestOptions): Promise<T> {
    const token = options.getToken?.()
    const headers: Record<string, string> = {
      Accept: 'application/json',
    }

    if (token) {
      headers.Authorization = `Bearer ${token}`
    }

    const init: RequestInit = {
      method: requestOptions.method,
      headers,
    }

    if (requestOptions.body !== undefined) {
      headers['Content-Type'] = 'application/json'
      init.body = JSON.stringify(requestOptions.body)
    }

    const response = await fetchImpl(`${baseUrl}${path}`, init)

    if (!response.ok) {
      const error = await parseError(response)
      if (response.status === 401) {
        options.onUnauthorized?.()
      }
      throw new ApiError(error)
    }

    if (response.status === 204) {
      return undefined as T
    }

    return (await response.json()) as T
  }

  return {
    get: <T>(path: string) => request<T>(path, { method: 'GET' }),
    post: <T>(path: string, body: unknown) => request<T>(path, { method: 'POST', body }),
    patch: <T>(path: string, body: unknown) => request<T>(path, { method: 'PATCH', body }),
    delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
  }
}

async function parseError(response: Response): Promise<BackendErrorResponse> {
  const fallback = {
    status: response.status,
    code: 'REQUEST_FAILED',
    message: `Request failed with status ${response.status}`,
    fieldErrors: [],
  }

  try {
    const data = (await response.json()) as Partial<BackendErrorResponse>
    return {
      status: data.status ?? response.status,
      code: data.code ?? fallback.code,
      message: data.message ?? fallback.message,
      path: data.path,
      timestamp: data.timestamp,
      fieldErrors: data.fieldErrors ?? [],
    }
  } catch {
    return fallback
  }
}

