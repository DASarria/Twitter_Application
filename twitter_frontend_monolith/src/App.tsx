import { useEffect, useMemo, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import './App.css'

type Post = {
  id: string
  authorSub: string
  authorName: string
  content: string
  createdAt: string
}

type MeResponse = {
  sub: string
  name: string
  email: string
  scopes: string[]
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const audience = import.meta.env.VITE_AUTH0_AUDIENCE
const defaultScope = 'openid profile email read:profile write:posts'

function App() {
  const {
    isAuthenticated,
    isLoading,
    user,
    error: auth0Error,
    loginWithPopup,
    logout,
    getAccessTokenSilently,
  } = useAuth0()

  const [posts, setPosts] = useState<Post[]>([])
  const [postDraft, setPostDraft] = useState('')
  const [myProfile, setMyProfile] = useState<MeResponse | null>(null)

  const [feedLoading, setFeedLoading] = useState(true)
  const [postSubmitting, setPostSubmitting] = useState(false)
  const [meLoading, setMeLoading] = useState(false)

  const [error, setError] = useState<string | null>(null)

  const remainingChars = useMemo(() => 140 - postDraft.length, [postDraft])

  const fetchPublicFeed = async (): Promise<Post[]> => {
    const response = await fetch(`${apiBaseUrl}/api/posts`)
    if (!response.ok) {
      throw new Error(`Public feed request failed (${response.status})`)
    }
    return (await response.json()) as Post[]
  }

  const refreshPublicFeed = async () => {
    try {
      const data = await fetchPublicFeed()
      setPosts(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error loading feed')
    } finally {
      setFeedLoading(false)
    }
  }

  useEffect(() => {
    void (async () => {
      try {
        const data = await fetchPublicFeed()
        setPosts(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error loading feed')
      } finally {
        setFeedLoading(false)
      }
    })()
  }, [])

  const getToken = async (scope?: string) => {
    return getAccessTokenSilently({
      authorizationParams: {
        audience,
        scope,
      },
    })
  }

  const loadMyProfile = async () => {
    try {
      setMeLoading(true)
      setError(null)

      const accessToken = await getToken('read:profile')
      const response = await fetch(`${apiBaseUrl}/api/me`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      })

      if (!response.ok) {
        throw new Error(`Profile request failed (${response.status})`)
      }

      const data = (await response.json()) as MeResponse
      setMyProfile(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error loading profile')
    } finally {
      setMeLoading(false)
    }
  }

  const createPost = async () => {
    if (!postDraft.trim()) {
      setError('Post content is required')
      return
    }

    if (postDraft.trim().length > 140) {
      setError('Post content must be at most 140 characters')
      return
    }

    try {
      setPostSubmitting(true)
      setError(null)

      const accessToken = await getToken('write:posts')
      const response = await fetch(`${apiBaseUrl}/api/posts`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content: postDraft.trim() }),
      })

      if (!response.ok) {
        const maybeErrorBody = await response.text()
        throw new Error(`Create post failed (${response.status}) ${maybeErrorBody}`)
      }

      setPostDraft('')
      setFeedLoading(true)
      await refreshPublicFeed()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error creating post')
    } finally {
      setPostSubmitting(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">

        <div className="auth-buttons">
          {!isAuthenticated && (
            <>
              <button
                onClick={() =>
                  void loginWithPopup({
                    authorizationParams: {
                      audience,
                      scope: defaultScope,
                    },
                  })
                }
                className="button button-primary"
              >
                Login with popup
              </button>
            </>
          )}

          {isAuthenticated && (
            <button
              className="button button-ghost"
              onClick={() =>
                logout({
                  logoutParams: {
                    returnTo: window.location.origin,
                  },
                })
              }
            >
              Logout
            </button>
          )}
        </div>
      </header>

      <main className="content-grid">
        <section className="panel panel-post">
          <h2>Crear Post</h2>
          {!isAuthenticated && (
            <p className="muted">
              Necesita estar autenticado para publicar nuevos mensajes.
            </p>
          )}

          {isAuthenticated && (
            <>
              <label htmlFor="post-content" className="label">
              </label>
              <textarea
                id="post-content"
                maxLength={220}
                value={postDraft}
                onChange={(event) => setPostDraft(event.target.value)}
                placeholder="¿Qué está pasando?"
              />
              <div className="composer-footer">
                <span className={remainingChars < 0 ? 'counter over' : 'counter'}>
                  {remainingChars} Caracteres restantes
                </span>
                <button
                  className="button button-primary"
                  onClick={() => void createPost()}
                  disabled={postSubmitting}
                >
                  {postSubmitting ? 'Publishing...' : 'Publish'}
                </button>
              </div>
            </>
          )}

          {isAuthenticated && (
            <div className="profile-actions">
              <button className="button button-ghost" onClick={() => void loadMyProfile()} disabled={meLoading}>
                {meLoading ? 'Loading profile...' : 'Load /api/me'}
              </button>
            </div>
          )}

          {myProfile && (
            <div className="profile-box">
              <p><strong>name:</strong> {myProfile.name ?? '-'}</p>
              <p><strong>email:</strong> {myProfile.email ?? '-'}</p>
            </div>
          )}
        </section>

        <section className="panel panel-feed">
          <div className="feed-header">
            <h2>Posts globales</h2>
            <button
              className="button button-ghost"
              onClick={() => {
                setFeedLoading(true)
                void refreshPublicFeed()
              }}
              disabled={feedLoading}
            >
              {feedLoading ? 'Refreshing...' : 'Refresh'}
            </button>
          </div>

          {isLoading && <p className="muted">Checking session...</p>}
          {auth0Error && <p className="error">Auth0 error: {auth0Error.message}</p>}
          {error && <p className="error">{error}</p>}

          {!feedLoading && posts.length === 0 && (
            <p className="muted">No posts yet. Publish the first message.</p>
          )}

          <ul className="feed-list">
            {posts.map((post) => (
              <li key={post.id} className="post-card overflow-hidden">
                <div className="post-head">
                  <strong>{post.authorName}</strong>
                  <time dateTime={post.createdAt}>
                    {new Date(post.createdAt).toLocaleString()}
                  </time>
                </div>
                <p>{post.content}</p>
              </li>
            ))}
          </ul>
        </section>
      </main>

      <footer className="footnote">
        <span>User: {user?.name ?? 'anonymous'}</span>
      </footer>
    </div>
  )
}

export default App
