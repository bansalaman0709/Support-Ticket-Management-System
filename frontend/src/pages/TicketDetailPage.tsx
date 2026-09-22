import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { addComment, changeTicketStatus, getTicket, updateTicket } from '../api/tickets'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingMessage } from '../components/LoadingMessage'
import { allowedNextStatuses } from '../status'
import type { TicketDetails, TicketStatus } from '../types'

export function TicketDetailPage() {
  const { id } = useParams()
  const ticketId = Number(id)

  const [ticket, setTicket] = useState<TicketDetails | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<unknown>(null)

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState('')
  const [assignee, setAssignee] = useState('')
  const [editError, setEditError] = useState<unknown>(null)
  const [editSaving, setEditSaving] = useState(false)

  const [statusError, setStatusError] = useState<unknown>(null)
  const [statusSaving, setStatusSaving] = useState(false)

  const [commentText, setCommentText] = useState('')
  const [commentError, setCommentError] = useState<unknown>(null)
  const [commentSaving, setCommentSaving] = useState(false)

  const load = useCallback(async () => {
    if (!Number.isFinite(ticketId)) {
      setLoadError(new Error('Invalid ticket id'))
      setLoading(false)
      return
    }
    setLoading(true)
    setLoadError(null)
    try {
      const data = await getTicket(ticketId)
      setTicket(data)
      setTitle(data.title)
      setDescription(data.description ?? '')
      setPriority(data.priority ?? '')
      setAssignee(data.assignee ?? '')
    } catch (err) {
      setLoadError(err)
      setTicket(null)
    } finally {
      setLoading(false)
    }
  }, [ticketId])

  useEffect(() => {
    void load()
  }, [load])

  async function onSaveFields(event: FormEvent) {
    event.preventDefault()
    setEditError(null)
    if (!title.trim()) {
      setEditError(new Error('Title is required.'))
      return
    }
    setEditSaving(true)
    try {
      const updated = await updateTicket(ticketId, {
        title: title.trim(),
        description,
        priority,
        assignee,
      })
      setTicket((prev) =>
        prev
          ? {
              ...prev,
              ...updated,
              comments: prev.comments,
            }
          : prev,
      )
    } catch (err) {
      setEditError(err)
    } finally {
      setEditSaving(false)
    }
  }

  async function onChangeStatus(next: TicketStatus) {
    setStatusError(null)
    setStatusSaving(true)
    try {
      const updated = await changeTicketStatus(ticketId, next)
      setTicket((prev) =>
        prev
          ? {
              ...prev,
              ...updated,
              comments: prev.comments,
            }
          : prev,
      )
    } catch (err) {
      setStatusError(err)
    } finally {
      setStatusSaving(false)
    }
  }

  async function onAddComment(event: FormEvent) {
    event.preventDefault()
    setCommentError(null)
    if (!commentText.trim()) {
      setCommentError(new Error('Comment text is required.'))
      return
    }
    setCommentSaving(true)
    try {
      const created = await addComment(ticketId, commentText)
      setTicket((prev) =>
        prev
          ? {
              ...prev,
              comments: [...prev.comments, created],
            }
          : prev,
      )
      setCommentText('')
    } catch (err) {
      setCommentError(err)
    } finally {
      setCommentSaving(false)
    }
  }

  if (loading) {
    return (
      <section className="page">
        <LoadingMessage label="Loading ticket…" />
      </section>
    )
  }

  if (loadError || !ticket) {
    return (
      <section className="page">
        <header className="page-header">
          <h1>Ticket</h1>
          <Link to="/">Back to list</Link>
        </header>
        <ErrorBanner error={loadError ?? new Error('Ticket not found')} title="Could not load ticket" />
      </section>
    )
  }

  const nextStatuses = allowedNextStatuses(ticket.status)

  return (
    <section className="page">
      <header className="page-header">
        <h1>
          Ticket #{ticket.id}
        </h1>
        <Link to="/">Back to list</Link>
      </header>

      <p>
        Status:{' '}
        <span className={`status status-${ticket.status}`}>{ticket.status}</span>
      </p>

      <section className="panel">
        <h2>Change status</h2>
        <ErrorBanner error={statusError} title="Status change failed" />
        {nextStatuses.length === 0 ? (
          <p className="empty" role="status">
            No further status transitions are available from {ticket.status}.
          </p>
        ) : (
          <div className="button-row">
            {nextStatuses.map((next) => (
              <button
                key={next}
                type="button"
                disabled={statusSaving}
                onClick={() => void onChangeStatus(next)}
              >
                Move to {next}
              </button>
            ))}
          </div>
        )}
      </section>

      <section className="panel">
        <h2>Edit fields</h2>
        <ErrorBanner error={editError} title="Update failed" />
        <form className="stack-form" onSubmit={onSaveFields}>
          <label>
            Title <span className="required">*</span>
            <input
              name="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              maxLength={200}
              required
            />
          </label>
          <label>
            Description
            <textarea
              name="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={5000}
              rows={5}
            />
          </label>
          <label>
            Priority
            <input
              name="priority"
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              maxLength={50}
            />
          </label>
          <label>
            Assignee
            <input
              name="assignee"
              value={assignee}
              onChange={(e) => setAssignee(e.target.value)}
              maxLength={100}
            />
          </label>
          <button type="submit" disabled={editSaving}>
            {editSaving ? 'Saving…' : 'Save changes'}
          </button>
        </form>
      </section>

      <section className="panel">
        <h2>Comments</h2>
        <ErrorBanner error={commentError} title="Could not add comment" />
        {ticket.comments.length === 0 ? (
          <p className="empty" role="status">
            No comments yet.
          </p>
        ) : (
          <ul className="comment-list">
            {ticket.comments.map((comment) => (
              <li key={comment.id}>
                <span className="comment-id">#{comment.id}</span>
                <p>{comment.text}</p>
              </li>
            ))}
          </ul>
        )}
        <form className="stack-form" onSubmit={onAddComment}>
          <label>
            New comment
            <textarea
              name="comment"
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              maxLength={2000}
              rows={3}
              required
            />
          </label>
          <button type="submit" disabled={commentSaving}>
            {commentSaving ? 'Adding…' : 'Add comment'}
          </button>
        </form>
      </section>
    </section>
  )
}
