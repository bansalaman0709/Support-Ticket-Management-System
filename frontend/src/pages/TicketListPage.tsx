import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { listTickets } from '../api/tickets'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingMessage } from '../components/LoadingMessage'
import { TICKET_STATUSES } from '../status'
import type { Ticket, TicketStatus } from '../types'

export function TicketListPage() {
  const [tickets, setTickets] = useState<Ticket[]>([])
  const [keyword, setKeyword] = useState('')
  const [appliedKeyword, setAppliedKeyword] = useState('')
  const [status, setStatus] = useState<TicketStatus | ''>('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<unknown>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await listTickets({
        q: appliedKeyword || undefined,
        status,
      })
      setTickets(data)
    } catch (err) {
      setError(err)
      setTickets([])
    } finally {
      setLoading(false)
    }
  }, [appliedKeyword, status])

  useEffect(() => {
    void load()
  }, [load])

  function onSearch(event: FormEvent) {
    event.preventDefault()
    setAppliedKeyword(keyword.trim())
  }

  function clearSearch() {
    setKeyword('')
    setAppliedKeyword('')
  }

  return (
    <section className="page">
      <header className="page-header">
        <h1>Tickets</h1>
        <Link className="button" to="/tickets/new">
          Create ticket
        </Link>
      </header>

      <form className="filters" onSubmit={onSearch}>
        <label>
          Search
          <input
            type="search"
            name="q"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Keyword"
            aria-label="Search by keyword"
          />
        </label>
        <label>
          Status
          <select
            name="status"
            value={status}
            onChange={(e) => setStatus(e.target.value as TicketStatus | '')}
            aria-label="Filter by status"
          >
            <option value="">All statuses</option>
            {TICKET_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </label>
        <button type="submit">Search</button>
        {appliedKeyword && (
          <button type="button" className="button-secondary" onClick={clearSearch}>
            Clear search
          </button>
        )}
      </form>

      <ErrorBanner error={error} title="Could not load tickets" />

      {loading && <LoadingMessage label="Loading tickets…" />}

      {!loading && !error && tickets.length === 0 && (
        <p className="empty" role="status">
          No tickets found.
        </p>
      )}

      {!loading && tickets.length > 0 && (
        <table className="ticket-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Status</th>
              <th>Priority</th>
              <th>Assignee</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map((ticket) => (
              <tr key={ticket.id}>
                <td>{ticket.id}</td>
                <td>
                  <Link to={`/tickets/${ticket.id}`}>{ticket.title}</Link>
                </td>
                <td>
                  <span className={`status status-${ticket.status}`}>{ticket.status}</span>
                </td>
                <td>{ticket.priority ?? '—'}</td>
                <td>{ticket.assignee ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
