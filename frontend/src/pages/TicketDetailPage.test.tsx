import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { TicketDetailPage } from '../pages/TicketDetailPage'
import * as ticketsApi from '../api/tickets'
import { ApiError } from '../types'

vi.mock('../api/tickets')

const baseTicket = {
  id: 7,
  title: 'Broken printer',
  description: 'Floor 2',
  priority: 'MED',
  assignee: 'bob',
  status: 'OPEN' as const,
  comments: [] as { id: number; text: string }[],
}

describe('TicketDetailPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  function renderDetail() {
    return render(
      <MemoryRouter initialEntries={['/tickets/7']}>
        <Routes>
          <Route path="/tickets/:id" element={<TicketDetailPage />} />
        </Routes>
      </MemoryRouter>,
    )
  }

  it('loads details, updates fields, adds comments, and transitions status', async () => {
    vi.mocked(ticketsApi.getTicket).mockResolvedValue({ ...baseTicket })
    vi.mocked(ticketsApi.updateTicket).mockResolvedValue({
      ...baseTicket,
      title: 'Broken printer fixed title',
      assignee: 'carol',
    })
    vi.mocked(ticketsApi.addComment).mockResolvedValue({
      id: 1,
      text: 'Looking into it',
    })
    vi.mocked(ticketsApi.changeTicketStatus).mockResolvedValue({
      ...baseTicket,
      title: 'Broken printer fixed title',
      assignee: 'carol',
      status: 'IN_PROGRESS',
    })

    renderDetail()

    expect(await screen.findByDisplayValue('Broken printer')).toBeInTheDocument()
    expect(screen.getByText(/no comments yet/i)).toBeInTheDocument()

    const titleInput = screen.getByDisplayValue('Broken printer')
    await userEvent.clear(titleInput)
    await userEvent.type(titleInput, 'Broken printer fixed title')
    const assigneeInput = screen.getByDisplayValue('bob')
    await userEvent.clear(assigneeInput)
    await userEvent.type(assigneeInput, 'carol')
    await userEvent.click(screen.getByRole('button', { name: /save changes/i }))

    await waitFor(() => {
      expect(ticketsApi.updateTicket).toHaveBeenCalledWith(7, {
        title: 'Broken printer fixed title',
        description: 'Floor 2',
        priority: 'MED',
        assignee: 'carol',
      })
    })

    await userEvent.type(screen.getByLabelText(/new comment/i), 'Looking into it')
    await userEvent.click(screen.getByRole('button', { name: /add comment/i }))
    expect(await screen.findByText('Looking into it')).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /move to in_progress/i }))
    await waitFor(() => {
      expect(ticketsApi.changeTicketStatus).toHaveBeenCalledWith(7, 'IN_PROGRESS')
    })
    expect(await screen.findByText('IN_PROGRESS')).toBeInTheDocument()
  })

  it('shows meaningful errors for invalid status transitions', async () => {
    vi.mocked(ticketsApi.getTicket).mockResolvedValue({
      ...baseTicket,
      status: 'CLOSED',
      comments: [],
    })

    const { unmount } = renderDetail()

    expect(
      await screen.findByText(/no further status transitions are available/i),
    ).toBeInTheDocument()
    unmount()

    vi.mocked(ticketsApi.getTicket).mockResolvedValue({ ...baseTicket })
    vi.mocked(ticketsApi.changeTicketStatus).mockRejectedValue(
      new ApiError(409, {
        message: 'Invalid status transition from OPEN to CLOSED',
        errors: [],
      }),
    )

    renderDetail()

    await screen.findByRole('button', { name: /move to in_progress/i })
    await userEvent.click(screen.getByRole('button', { name: /move to in_progress/i }))
    expect(
      await screen.findByText(/invalid status transition from open to closed/i),
    ).toBeInTheDocument()
  })

  it('shows loading then load error for missing tickets', async () => {
    let rejectGet!: (reason?: unknown) => void
    vi.mocked(ticketsApi.getTicket).mockImplementationOnce(
      () =>
        new Promise((_, reject) => {
          rejectGet = reject
        }),
    )

    renderDetail()
    expect(screen.getByRole('status')).toHaveTextContent(/loading ticket/i)

    rejectGet(
      new ApiError(404, {
        message: 'Ticket not found: 7',
        errors: [],
      }),
    )

    expect(await screen.findByText(/ticket not found: 7/i)).toBeInTheDocument()
  })

  it('shows meaningful errors for field update and comment failures', async () => {
    vi.mocked(ticketsApi.getTicket).mockResolvedValue({ ...baseTicket })
    vi.mocked(ticketsApi.updateTicket).mockRejectedValue(
      new ApiError(400, {
        message: 'Validation failed',
        errors: [{ field: 'title', message: 'must not be blank' }],
      }),
    )
    vi.mocked(ticketsApi.addComment).mockRejectedValue(
      new ApiError(400, {
        message: 'Validation failed',
        errors: [{ field: 'text', message: 'must not be blank' }],
      }),
    )

    renderDetail()
    await screen.findByDisplayValue('Broken printer')

    await userEvent.click(screen.getByRole('button', { name: /save changes/i }))
    expect(await screen.findByText(/validation failed/i)).toBeInTheDocument()
    expect(screen.getAllByText(/title: must not be blank/i).length).toBeGreaterThan(0)

    await userEvent.type(screen.getByLabelText(/new comment/i), 'Retry note')
    await userEvent.click(screen.getByRole('button', { name: /add comment/i }))
    expect(screen.getAllByText(/text: must not be blank/i).length).toBeGreaterThan(0)
    expect(screen.getByText(/could not add comment/i)).toBeInTheDocument()
  })
})
