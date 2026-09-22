import type { ApiError } from '../types'
import { formatApiError } from '../api/client'

interface ErrorBannerProps {
  error: unknown
  title?: string
}

export function ErrorBanner({ error, title = 'Error' }: ErrorBannerProps) {
  if (!error) {
    return null
  }

  const message = formatApiError(error)
  const apiError = error as ApiError
  const fieldErrors =
    apiError?.body?.errors?.filter((e) => e?.message) ?? []

  return (
    <div className="error-banner" role="alert">
      <strong>{title}</strong>
      <p>{message}</p>
      {fieldErrors.length > 0 && (
        <ul>
          {fieldErrors.map((e, index) => (
            <li key={`${e.field ?? 'field'}-${index}`}>
              {e.field ? `${e.field}: ${e.message}` : e.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
