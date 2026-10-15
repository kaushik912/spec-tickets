import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import App from './App'

type Todo = { id: number; title: string; completed: boolean; createdAt: string }

let todos: Todo[]

beforeEach(() => {
  todos = []
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string, init?: RequestInit) => {
      if (init?.method === 'POST') {
        const { title } = JSON.parse(init.body as string)
        const todo = { id: todos.length + 1, title, completed: false, createdAt: '' }
        todos.push(todo)
        return new Response(JSON.stringify(todo), { status: 201 })
      }
      if (init?.method === 'PATCH') {
        const id = Number(url.split('/').pop())
        const todo = todos.find((t) => t.id === id)!
        todo.completed = JSON.parse(init.body as string).completed
        return new Response(JSON.stringify(todo), { status: 200 })
      }
      if (init?.method === 'DELETE') {
        if (url.endsWith('/completed')) {
          const deleted = todos.filter((t) => t.completed).length
          todos = todos.filter((t) => !t.completed)
          return new Response(JSON.stringify({ deleted }), { status: 200 })
        }
        const id = Number(url.split('/').pop())
        todos = todos.filter((t) => t.id !== id)
        return new Response(null, { status: 204 })
      }
      const filter = new URL(url, 'http://x').searchParams.get('filter')
      const out =
        filter === 'active' ? todos.filter((t) => !t.completed)
        : filter === 'completed' ? todos.filter((t) => t.completed)
        : todos
      return new Response(JSON.stringify(out), { status: 200 })
    }),
  )
})

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

test('shows existing todos and items-left count', async () => {
  todos = [
    { id: 1, title: 'one', completed: false, createdAt: '' },
    { id: 2, title: 'two', completed: true, createdAt: '' },
  ]
  render(<App />)
  expect(await screen.findByText('one')).toBeTruthy()
  expect(screen.getByText('two')).toBeTruthy()
  expect(screen.getByText('1 item left')).toBeTruthy()
})

test('Enter adds a todo, clears input and shows it', async () => {
  const user = userEvent.setup()
  render(<App />)
  const input = screen.getByPlaceholderText('What needs to be done?') as HTMLInputElement
  await user.type(input, 'Buy milk{Enter}')
  expect(await screen.findByText('Buy milk')).toBeTruthy()
  expect(input.value).toBe('')
  expect(screen.getByText('1 item left')).toBeTruthy()
})

test('blank input adds nothing', async () => {
  const user = userEvent.setup()
  render(<App />)
  await waitFor(() => expect(fetch).toHaveBeenCalled())
  await user.type(screen.getByPlaceholderText('What needs to be done?'), '   {Enter}')
  expect(vi.mocked(fetch).mock.calls.filter(([, init]) => init?.method === 'POST')).toHaveLength(0)
})

const mixed = (): Todo[] => [
  { id: 1, title: 'one', completed: false, createdAt: '' },
  { id: 2, title: 'two', completed: true, createdAt: '' },
]

test('filter links show only matching todos and highlight selected', async () => {
  todos = mixed()
  const user = userEvent.setup()
  render(<App />)
  expect(await screen.findByText('two')).toBeTruthy()
  expect(screen.getByText('All').className).toContain('selected')
  await user.click(screen.getByText('Active'))
  await waitFor(() => expect(screen.queryByText('two')).toBeNull())
  expect(screen.getByText('one')).toBeTruthy()
  expect(screen.getByText('Active').className).toContain('selected')
  expect(screen.getByText('All').className).not.toContain('selected')
  await user.click(screen.getByText('Completed'))
  await waitFor(() => expect(screen.queryByText('one')).toBeNull())
  expect(screen.getByText('two')).toBeTruthy()
})

test('Clear completed shown only when completed exist and removes them', async () => {
  todos = mixed()
  const user = userEvent.setup()
  render(<App />)
  const btn = await screen.findByText('Clear completed')
  await user.click(btn)
  await waitFor(() => expect(screen.queryByText('two')).toBeNull())
  expect(screen.getByText('one')).toBeTruthy()
  expect(screen.queryByText('Clear completed')).toBeNull()
})

test('no Clear completed button when nothing completed', async () => {
  todos = [{ id: 1, title: 'one', completed: false, createdAt: '' }]
  render(<App />)
  await screen.findByText('one')
  expect(screen.queryByText('Clear completed')).toBeNull()
})

test('checkbox toggles completed, strikethrough class and count', async () => {
  const user = userEvent.setup()
  todos = [{ id: 1, title: 'one', completed: false, createdAt: '' }]
  render(<App />)
  const box = (await screen.findByRole('checkbox')) as HTMLInputElement
  await user.click(box)
  await screen.findByText('0 items left')
  expect(screen.getByText('one').closest('li')!.className).toContain('completed')
  expect((screen.getByRole('checkbox') as HTMLInputElement).checked).toBe(true)
  await user.click(screen.getByRole('checkbox'))
  await screen.findByText('1 item left')
  expect(screen.getByText('one').closest('li')!.className).not.toContain('completed')
})

test('delete button removes item and updates count', async () => {
  const user = userEvent.setup()
  todos = [
    { id: 1, title: 'one', completed: false, createdAt: '' },
    { id: 2, title: 'two', completed: false, createdAt: '' },
  ]
  render(<App />)
  await screen.findByText('2 items left')
  await user.click(screen.getAllByRole('button', { name: 'Delete' })[0])
  await screen.findByText('1 item left')
  expect(screen.queryByText('one')).toBeNull()
  expect(screen.getByText('two')).toBeTruthy()
})
