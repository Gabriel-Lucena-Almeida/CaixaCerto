import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Logo } from './Logo';

export function Layout() {
  const { user } = useAuth();

  return (
    <div className="app-container">
      <header className="header p-5 border-b border-gray-100 bg-white sticky top-0 z-10">
        <div className="logo">
          <Logo />
          <span className="logo-text hidden sm:block">CaixaCerto</span>
        </div>
        <span className="text-sm text-gray-500 hidden sm:block">
          Olá, {user?.firstName || 'Usuário'}
        </span>
      </header>

      <main className="page-container">
        <Outlet />
      </main>

      <footer className="footer">
        <button className="footer-link" onClick={() => {}}>Sair</button>
        <span aria-hidden="true">·</span>
        <a href="/configuracoes" className="footer-link">Configurações</a>
      </footer>
    </div>
  );
}