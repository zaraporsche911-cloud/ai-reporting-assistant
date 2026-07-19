import { useMutation, useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { profileApi } from '../api'
import { getProblemMessage } from '../api/client'
import { useToast } from '../components/Toast'
import { Button, ErrorState, Field, inputClass, LoadingState, PageHeader, Panel } from '../components/ui'

export function SettingsPage() {
  const query = useQuery({ queryKey: ['preferences'], queryFn: profileApi.preferences })
  const toast = useToast(); const [dark, setDark] = useState(false); const [timezone, setTimezone] = useState(Intl.DateTimeFormat().resolvedOptions().timeZone)
  useEffect(() => { if (query.data) { setDark(query.data.darkMode); setTimezone(query.data.timezone) } }, [query.data])
  const update = useMutation({ mutationFn: () => profileApi.updatePreferences(dark, timezone), onSuccess: () => { document.documentElement.classList.toggle('dark', dark); localStorage.setItem('reporting-theme', dark ? 'dark' : 'light'); toast.success('Preferences saved') }, onError: (error) => toast.error(getProblemMessage(error)) })
  if (query.isLoading) return <LoadingState label="Loading settings" />
  if (query.isError) return <ErrorState message={getProblemMessage(query.error)} />
  return <><PageHeader eyebrow="Preferences" title="Settings" description="Personal display preferences are stored per user; sensitive system configuration is managed by environment variables." /><Panel className="max-w-2xl"><form className="space-y-6" onSubmit={(event) => { event.preventDefault(); update.mutate() }}><label className="flex items-center justify-between gap-4 rounded-xl border border-line p-4 dark:border-slate-700"><div><p className="font-semibold">Dark mode</p><p className="mt-1 text-sm text-ink-600 dark:text-slate-400">Use a darker visual theme for this workspace.</p></div><input type="checkbox" className="size-5 accent-brand-700" checked={dark} onChange={(event) => setDark(event.target.checked)} /></label><Field label="Timezone"><input className={inputClass} value={timezone} onChange={(event) => setTimezone(event.target.value)} placeholder="Europe/Paris" required /></Field><Button disabled={update.isPending}>Save preferences</Button></form></Panel></>
}
