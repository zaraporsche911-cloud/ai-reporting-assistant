import { Bar, BarChart, CartesianGrid, Cell, Legend, Line, LineChart, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { GeneratedReport, ReportResult } from '../types/api'
import { EmptyState, Panel, StatusBadge } from './ui'

const colors = ['#0b6b59', '#6356a5', '#e8a128', '#3b82f6', '#db5d50']

export function ReportView({ report, compact = false }: { report: GeneratedReport; compact?: boolean }) {
  if (report.status === 'FAILED') return <Panel><StatusBadge tone="danger">Failed</StatusBadge><p className="mt-3 text-sm text-red-700 dark:text-red-300">{report.errorDetail ?? 'The report could not be generated.'}</p></Panel>
  if (!report.result) return <EmptyState title="No report result" description="This execution did not produce structured report data." />
  const result = report.result
  return <div className="space-y-5">
    <div className="flex flex-wrap gap-2"><StatusBadge>{report.reportType.replaceAll('_', ' ')}</StatusBadge><StatusBadge>{result.from} to {result.to}</StatusBadge><StatusBadge tone="neutral">{report.executionTimeMs} ms</StatusBadge>{report.intent.groupBy !== 'NONE' && <StatusBadge tone="neutral">Grouped by {report.intent.groupBy.toLowerCase()}</StatusBadge>}</div>
    {!compact && <Panel className="border-l-4 border-l-ai-700"><div className="flex flex-wrap items-center gap-2"><StatusBadge tone="ai">AI-assisted explanation</StatusBadge><StatusBadge tone="neutral">Data: {result.dataSource}</StatusBadge></div><p className="mt-3 text-sm leading-7 text-ink-800 dark:text-slate-300">{report.summary ?? 'A deterministic report was generated from validated fleet data.'}</p></Panel>}
    {result.kpis.length > 0 && <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{result.kpis.map((kpi) => <Panel key={kpi.label}><p className="text-xs font-bold uppercase tracking-wider text-ink-600 dark:text-slate-400">{kpi.label}</p><p className="mt-3 text-2xl font-bold tracking-tight">{kpi.value}<span className="ml-1 text-sm font-medium text-ink-600">{kpi.unit}</span></p>{kpi.trend && <p className="mt-2 text-xs text-brand-700 dark:text-emerald-400">{kpi.trend}</p>}</Panel>)}</div>}
    {result.chart && result.chart.points.length > 0 && <Panel><h3 className="mb-5 font-bold">{result.title}</h3><div className="h-80" aria-label={`${result.visualization} visualization`}><ReportChart result={result} /></div></Panel>}
    {result.rows.length > 0 && <Panel className="overflow-hidden p-0"><div className="overflow-x-auto"><table className="w-full min-w-180 text-left text-sm"><thead className="bg-slate-50 text-xs uppercase tracking-wide text-ink-600 dark:bg-slate-800 dark:text-slate-400"><tr>{result.columns.map((column) => <th className="px-5 py-3.5 font-bold" key={column.key}>{column.label}{column.unit ? ` (${column.unit})` : ''}</th>)}</tr></thead><tbody className="divide-y divide-line dark:divide-slate-800">{result.rows.map((row, index) => <tr key={index} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/50">{result.columns.map((column) => <td className="whitespace-nowrap px-5 py-3.5" key={column.key}>{formatValue(row[column.key])}</td>)}</tr>)}</tbody></table></div></Panel>}
    {result.notices.length > 0 && <div className="rounded-xl bg-amber-50 px-4 py-3 text-sm text-amber-900 dark:bg-amber-950/40 dark:text-amber-200">{result.notices.join(' ')}</div>}
  </div>
}

function ReportChart({ result }: { result: ReportResult }) {
  const chart = result.chart!
  if (result.visualization === 'DONUT_CHART') return <ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={chart.points} dataKey={chart.valueKeys[0]} nameKey={chart.categoryKey} innerRadius="50%" outerRadius="78%" paddingAngle={2}>{chart.points.map((_, index) => <Cell key={index} fill={colors[index % colors.length]} />)}</Pie><Tooltip /><Legend /></PieChart></ResponsiveContainer>
  if (result.visualization === 'LINE_CHART' || result.visualization === 'AREA_CHART') return <ResponsiveContainer width="100%" height="100%"><LineChart data={chart.points} margin={{ left: 5, right: 20 }}><CartesianGrid strokeDasharray="3 3" vertical={false} /><XAxis dataKey={chart.categoryKey} tick={{ fontSize: 12 }} /><YAxis tick={{ fontSize: 12 }} /><Tooltip /><Legend />{chart.valueKeys.map((key, index) => <Line key={key} type="monotone" dataKey={key} stroke={colors[index % colors.length]} strokeWidth={2.5} dot={false} />)}</LineChart></ResponsiveContainer>
  return <ResponsiveContainer width="100%" height="100%"><BarChart data={chart.points} margin={{ left: 5, right: 20 }}><CartesianGrid strokeDasharray="3 3" vertical={false} /><XAxis dataKey={chart.categoryKey} tick={{ fontSize: 12 }} /><YAxis tick={{ fontSize: 12 }} /><Tooltip /><Legend />{chart.valueKeys.map((key, index) => <Bar key={key} dataKey={key} fill={colors[index % colors.length]} radius={[5, 5, 0, 0]} />)}</BarChart></ResponsiveContainer>
}

function formatValue(value: unknown) {
  if (value === null || value === undefined) return '—'
  if (typeof value === 'number') return new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 }).format(value)
  return String(value)
}
