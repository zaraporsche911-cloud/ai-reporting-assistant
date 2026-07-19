import { Component, type ErrorInfo, type ReactNode } from 'react'
import { Button } from './ui'

export class ErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false }
  static getDerivedStateFromError() { return { failed: true } }
  componentDidCatch(error: Error, info: ErrorInfo) { console.error('Unhandled UI error', error, info.componentStack) }
  render() {
    if (this.state.failed) return <main className="grid min-h-screen place-items-center p-6"><div className="max-w-md text-center"><p className="text-5xl">Something went wrong</p><p className="mt-4 text-ink-600">The interface encountered an unexpected error. Your data has not been changed.</p><Button className="mt-6" onClick={() => window.location.assign('/')}>Return to dashboard</Button></div></main>
    return this.props.children
  }
}
