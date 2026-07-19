import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { GeneratedReport } from '../types/api'
import { ReportView } from './ReportView'

describe('ReportView', () => {
  it('renders deterministic KPIs and source notices', () => {
    const report = {
      reportType: 'FLEET_OVERVIEW', status: 'SUCCEEDED', summary: 'Fleet utilization remains healthy.',
      executionTimeMs: 14, intent: { groupBy: 'NONE' },
      result: { title: 'Fleet overview', dataSource: 'MOCK', from: '2026-07-01', to: '2026-07-19', visualization: 'KPI', kpis: [{ label: 'Active vehicles', value: '8', unit: null, trend: null }], columns: [], rows: [], chart: null, notices: ['Demonstration data'] },
    } as unknown as GeneratedReport
    render(<ReportView report={report} />)
    expect(screen.getByText('Active vehicles')).toBeInTheDocument()
    expect(screen.getByText('Fleet utilization remains healthy.')).toBeInTheDocument()
    expect(screen.getByText('Demonstration data')).toBeInTheDocument()
  })
})
