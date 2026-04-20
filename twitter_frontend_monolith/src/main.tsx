import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Auth0Provider } from '@auth0/auth0-react'
import './index.css'
import App from './App.tsx'

const domain = import.meta.env.VITE_AUTH0_DOMAIN
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID
const audience = import.meta.env.VITE_AUTH0_AUDIENCE

if (!domain || !clientId || !audience) {
  throw new Error(
    'Missing Auth0 environment variables. Define VITE_AUTH0_DOMAIN, VITE_AUTH0_CLIENT_ID, and VITE_AUTH0_AUDIENCE.'
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        audience,
        redirect_uri: window.location.origin,
      }}
      useRefreshTokens={true}
      useRefreshTokensFallback={true}
      cacheLocation="localstorage"
    >
      <App />
    </Auth0Provider>
  </StrictMode>,
)
