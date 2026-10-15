import { useEffect, useState, type KeyboardEvent } from 'react'
import { clearCompleted, createTodo, deleteTodo, listTodos, updateTodo, type Filter, type Todo } from './api'
import './index.css'

function App() {
  const [todos, setTodos] = useState<Todo[]>([])
  const [title, setTitle] = useState('')

  const [filter, setFilter] = useState<Filter>('all')
  const [hasCompleted, setHasCompleted] = useState(false)

  function applyList(f: Filter, list: Todo[]) {
    setTodos(list)
    if (f === 'completed') setHasCompleted(list.length > 0)
    else if (f === 'all') setHasCompleted(list.some((t) => t.completed))
  }

  async function refresh(f: Filter) {
    applyList(f, await listTodos(f))
  }

  useEffect(() => {
    listTodos(filter)
      .then((list) => applyList(filter, list))
      .catch(() => setTodos([]))
  }, [filter])

  async function onKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key !== 'Enter') return
    const trimmed = title.trim()
    if (!trimmed) return
    const todo = await createTodo(trimmed)
    if (filter !== 'completed') setTodos((prev) => [...prev, todo])
    setTitle('')
  }

  async function onClearCompleted() {
    await clearCompleted()
    setHasCompleted(false)
    await refresh(filter)
  }

  async function onToggle(t: Todo) {
    await updateTodo(t.id, !t.completed)
    await refresh(filter)
  }

  async function onDelete(id: number) {
    await deleteTodo(id)
    await refresh(filter)
  }

  const left = todos.filter((t) => !t.completed).length

  return (
    <section className="todoapp">
      <h1>todos</h1>
      <input
        className="new-todo"
        placeholder="What needs to be done?"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={onKeyDown}
        autoFocus
      />
      <ul className="todo-list">
        {todos.map((t) => (
          <li key={t.id} className={t.completed ? 'completed' : ''}>
            <input
              type="checkbox"
              className="toggle"
              checked={t.completed}
              onChange={() => onToggle(t)}
              aria-label={`Toggle ${t.title}`}
            />
            <span>{t.title}</span>
            <button className="destroy" aria-label="Delete" onClick={() => onDelete(t.id)}>
              ×
            </button>
          </li>
        ))}
      </ul>
      <footer>
        <span className="todo-count">
          {left} {left === 1 ? 'item' : 'items'} left
        </span>
        <ul className="filters">
          {(['all', 'active', 'completed'] as Filter[]).map((f) => (
            <li key={f}>
              <a
                href={`#/${f === 'all' ? '' : f}`}
                className={filter === f ? 'selected' : ''}
                onClick={(e) => {
                  e.preventDefault()
                  setFilter(f)
                }}
              >
                {f[0].toUpperCase() + f.slice(1)}
              </a>
            </li>
          ))}
        </ul>
        {hasCompleted && (
          <button className="clear-completed" onClick={onClearCompleted}>
            Clear completed
          </button>
        )}
      </footer>
    </section>
  )
}

export default App
