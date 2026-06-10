import { describe, expect, it, vi } from 'vitest'

import { ApiError, createApiClient } from './client'

describe('api client', () => {
  it('attaches bearer token and serializes JSON bodies', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'Content-Type': 'application/json' }),
      json: async () => ({ id: 'acc-1' }),
    })
    const client = createApiClient({
      baseUrl: 'http://localhost:8080/api/v1',
      getToken: () => 'token-123',
      fetchImpl,
    })

    const result = await client.post<{ id: string }>('/accounts', { name: 'Cash' })

    expect(result.id).toBe('acc-1')
    expect(fetchImpl).toHaveBeenCalledWith('http://localhost:8080/api/v1/accounts', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        Authorization: 'Bearer token-123',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name: 'Cash' }),
    })
  })

  it('throws backend api errors with field details', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      headers: new Headers({ 'Content-Type': 'application/json' }),
      json: async () => ({
        status: 400,
        code: 'VALIDATION_FAILED',
        message: 'Request validation failed',
        fieldErrors: [{ field: 'name', message: 'must not be blank' }],
      }),
    })
    const client = createApiClient({ baseUrl: 'http://api.test/api/v1', fetchImpl })

    await expect(client.get('/accounts')).rejects.toMatchObject({
      name: 'ApiError',
      status: 400,
      code: 'VALIDATION_FAILED',
      message: 'Request validation failed',
      fieldErrors: [{ field: 'name', message: 'must not be blank' }],
    } satisfies Partial<ApiError>)
  })

  it('notifies callers when the backend returns unauthorized', async () => {
    const onUnauthorized = vi.fn()
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      headers: new Headers({ 'Content-Type': 'application/json' }),
      json: async () => ({ status: 401, code: 'UNAUTHORIZED', message: 'Authentication is required' }),
    })
    const client = createApiClient({ baseUrl: 'http://api.test/api/v1', fetchImpl, onUnauthorized })

    await expect(client.get('/users/me')).rejects.toBeInstanceOf(ApiError)

    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })
})
