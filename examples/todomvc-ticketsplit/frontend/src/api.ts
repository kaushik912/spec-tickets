export type Todo = { id: number; title: string; completed: boolean; createdAt: string }

export type Filter = 'all' | 'active' | 'completed'

export async function listTodos(filter: Filter = 'all'): Promise<Todo[]> {
  const res = await fetch(`/api/todos?filter=${filter}`)
  if (!res.ok) throw new Error('failed to load todos')
  return res.json()
}

export async function createTodo(title: string): Promise<Todo> {
  const res = await fetch('/api/todos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  })
  if (!res.ok) throw new Error('failed to create todo')
  return res.json()
}

export async function clearCompleted(): Promise<{ deleted: number }> {
  const res = await fetch('/api/todos/completed', { method: 'DELETE' })
  if (!res.ok) throw new Error('failed to clear completed')
  return res.json()
}

export async function updateTodo(id: number, completed: boolean): Promise<Todo> {
  const res = await fetch(`/api/todos/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ completed }),
  })
  if (!res.ok) throw new Error('failed to update todo')
  return res.json()
}

export async function deleteTodo(id: number): Promise<void> {
  const res = await fetch(`/api/todos/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('failed to delete todo')
}
