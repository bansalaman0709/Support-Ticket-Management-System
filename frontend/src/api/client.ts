import { ApiError, type ApiErrorBody } from '../types'

async function parseError(response: Response): Promise<ApiError> {
  let body: ApiErrorBody = {
    message: `Request failed (${response.status})`,
    errors: [],
  }
  try {
    const json = (await response.json()) as ApiErrorBody
    if (json && typeof json.message === 'string') {
      body = {
        message: json.message,
        errors: Array.isArray(json.errors) ? json.errors : [],
      }
    }
  } catch {
    // non-JSON error body — keep fallback message
  }
  return new ApiError(response.status, body)
}

export async function apiRequest<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
  })

  if (!response.ok) {
    throw await parseError(response)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export function formatApiError(error: unknown): string {
  if (error instanceof ApiError) {
    const fieldParts = (error.body.errors ?? [])
      .filter((e) => e?.message)
      .map((e) => (e.field ? `${e.field}: ${e.message}` : e.message))
    if (fieldParts.length > 0) {
      return `${error.body.message}. ${fieldParts.join('; ')}`
    }
    return error.body.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'Something went wrong'
}
