import { describe, expect, it } from 'vitest'
import { allowedNextStatuses } from './status'

describe('allowedNextStatuses', () => {
  it('matches the approved state-machine edges', () => {
    expect(allowedNextStatuses('OPEN')).toEqual(['IN_PROGRESS', 'CANCELLED'])
    expect(allowedNextStatuses('IN_PROGRESS')).toEqual(['RESOLVED', 'CANCELLED'])
    expect(allowedNextStatuses('RESOLVED')).toEqual(['CLOSED'])
    expect(allowedNextStatuses('CLOSED')).toEqual([])
    expect(allowedNextStatuses('CANCELLED')).toEqual([])
  })
})
