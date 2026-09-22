import { describe, expect, it } from 'vitest'
import { formatApiError } from './client'
import { ApiError } from '../types'

describe('formatApiError', () => {
  it('surfaces API message and field errors', () => {
    const error = new ApiError(400, {
      message: 'Validation failed',
      errors: [{ field: 'title', message: 'must not be blank' }],
    })
    expect(formatApiError(error)).toContain('Validation failed')
    expect(formatApiError(error)).toContain('title: must not be blank')
  })

  it('surfaces transition rejection messages', () => {
    const error = new ApiError(409, {
      message: 'Invalid status transition from CLOSED to OPEN',
      errors: [],
    })
    expect(formatApiError(error)).toBe(
      'Invalid status transition from CLOSED to OPEN',
    )
  })
})
