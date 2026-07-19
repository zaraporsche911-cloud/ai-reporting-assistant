import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { AuthContext } from '../auth/AuthStore'
import { ProtectedRoute } from './Guards'

describe('ProtectedRoute', () => {
  it('redirects an unauthenticated visitor to login', () => {
    const context = { user: null, isAuthenticated: false, login: async () => {}, register: async () => {}, logout: () => {}, refreshUser: async () => {} }
    render(<AuthContext.Provider value={context}><MemoryRouter initialEntries={['/']}><Routes><Route element={<ProtectedRoute />}><Route index element={<p>Private</p>} /></Route><Route path="/login" element={<p>Login</p>} /></Routes></MemoryRouter></AuthContext.Provider>)
    expect(screen.getByText('Login')).toBeInTheDocument()
  })
})
