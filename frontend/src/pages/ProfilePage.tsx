import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { profileApi } from '../api'
import { getProblemMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { useToast } from '../components/Toast'
import { Button, ErrorState, Field, inputClass, LoadingState, PageHeader, Panel, StatusBadge } from '../components/ui'

export function ProfilePage() {
  const query = useQuery({ queryKey: ['profile'], queryFn: profileApi.get })
  if (query.isLoading) return <LoadingState label="Loading profile" />
  if (query.isError || !query.data) return <ErrorState message={getProblemMessage(query.error)} />
  return <ProfileForm initialName={query.data.fullName} email={query.data.email} role={query.data.role} />
}
function ProfileForm({ initialName, email, role }: { initialName: string; email: string; role: string }) { const [name, setName] = useState(initialName); const toast = useToast(); const client = useQueryClient(); const { refreshUser } = useAuth(); const update = useMutation({ mutationFn: () => profileApi.update(name), onSuccess: async () => { await Promise.all([client.invalidateQueries({ queryKey: ['profile'] }), refreshUser()]); toast.success('Profile updated') }, onError: (error) => toast.error(getProblemMessage(error)) }); return <><PageHeader eyebrow="Account" title="Profile" description="Keep the identity attached to reports and audit events accurate." /><Panel className="max-w-2xl"><form className="space-y-5" onSubmit={(event) => { event.preventDefault(); update.mutate() }}><Field label="Full name"><input className={inputClass} value={name} onChange={(event) => setName(event.target.value)} minLength={2} maxLength={120} required /></Field><Field label="Email"><input className={`${inputClass} opacity-70`} value={email} disabled /></Field><div><p className="mb-2 text-sm font-semibold">Role</p><StatusBadge>{role.replaceAll('_', ' ')}</StatusBadge></div><Button disabled={update.isPending || name.trim().length < 2}>Save profile</Button></form></Panel></> }
