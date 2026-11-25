import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { Plus, LogOut, Trash2, ChevronRight } from 'lucide-react';
import { useMovements, useRecentMovements, useMonthSummary } from '../api/hooks';
import { useAuth } from '../auth/AuthContext';
import { format, formatInteger } from '../utils/money';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import { Logo } from '../ui/Logo';
import { Alert } from '../ui/Alert';
import type { MovementType } from '../api/types';

export function HomePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const { user, logout } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const aba = (searchParams.get('aba') as MovementType) || 'NEGOCIO';
  const isNegocio = aba === 'NEGOCIO';

  const { data: summary } = useMonthSummary();
  const { data: recentMovements } = useRecentMovements(aba);

  const destaque = isNegocio ? summary?.lucroReal ?? 0 : summary?.saldoPessoal ?? 0;
  const entradas = isNegocio ? summary?.entradasNegocio ?? 0 : summary?.entradasPessoal ?? 0;
  const saidas = isNegocio ? summary?.saidasNegocio ?? 0 : summary?.saidasPessoal ?? 0;

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      setError('Erro ao sair. Tente novamente.');
    }
  };

  const handleDeleteAccount = async () => {
    if (window.confirm('Isso apaga a sua conta e TODAS as suas movimentações. Não dá para desfazer. Tem certeza?')) {
      try {
        // TODO: implementar exclusão de conta
        alert('Funcionalidade em desenvolvimento');
      } catch (err) {
        setError('Erro ao excluir conta.');
      }
    }
  };

  return (
    <div className="flex-1 flex flex-col">
      <header className="header p-5 border-b border-gray-100 bg-white sticky top-0 z-10">
        <div className="flex items-center justify-between">
          <div>
            <span className="mes text-sm text-gray-500 font-normal">
              {new Date().toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })}
            </span>
            <h1 className="text-2xl font-heading font-bold">Seu caixa</h1>
          </div>
          <Logo />
        </div>
      </header>

      <main className="page-container">
        {error && <Alert message={error} onDismiss={() => setError(null)} />}

        <nav className="tabs" aria-label="Tipo de caixa">
          <button
            onClick={() => setSearchParams({ aba: 'NEGOCIO' })}
            className={`tab ${isNegocio ? 'tab-active' : 'tab-inactive'}`}
            aria-current={isNegocio ? 'page' : undefined}
          >
            Negócio
          </button>
          <button
            onClick={() => setSearchParams({ aba: 'PESSOAL' })}
            className={`tab ${!isNegocio ? 'tab-active' : 'tab-inactive'}`}
            aria-current={!isNegocio ? 'page' : undefined}
          >
            Pessoal
          </button>
        </nav>

        <Card variant="dark" padding="lg" className="summary-card">
          <span className="rotulo text-gray-300">
            {isNegocio ? 'Sobrou de verdade este mês' : 'Saldo pessoal do mês'}
          </span>
          <strong className="destaque text-white">{format(destaque)}</strong>
          <div className="duas flex gap-3">
            <div className="flex-1 bg-white/10 rounded-xl p-3 flex flex-col gap-0.5">
              <span className="text-xs text-gray-400">Entradas</span>
              <b className="text-lg text-primary-400">{formatInteger(entradas)}</b>
            </div>
            <div className="flex-1 bg-white/10 rounded-xl p-3 flex flex-col gap-0.5">
              <span className="text-xs text-gray-400">Saídas</span>
              <b className="text-lg text-accent-redLight">{formatInteger(saidas)}</b>
            </div>
          </div>
        </Card>

        <div className="linha flex items-center justify-between">
          <h2 className="text-lg font-heading font-bold">Últimas movimentações</h2>
          <Link to="/resumo" className="btn-link text-sm">Ver resumo</Link>
        </div>

        <Card padding="none">
          {recentMovements && recentMovements.length > 0 ? (
            <ul className="lista divide-y divide-gray-100">
              {recentMovements.map((movement) => (
                <li key={movement.id} className="list-item">
                  <div className="list-item-content">
                    <span className="titulo-item">{movement.description}</span>
                    <span className="detalhe">
                      {movement.type === 'NEGOCIO' ? 'Negócio' : 'Pessoal'} ·{' '}
                      {new Date(movement.date).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })}
                    </span>
                  </div>
                  <b className={`list-item-amount ${movement.nature === 'ENTRADA' ? 'amount-positive' : 'amount-negative'}`}>
                    {movement.nature === 'ENTRADA' ? '+ ' : '− '}{format(movement.amountCents)}
                  </b>
                </li>
              ))}
            </ul>
          ) : (
            <div className="empty-state">
              <p>Nenhuma movimentação ainda. Registre a primeira.</p>
            </div>
          )}
        </Card>

        <Link to="/registrar">
          <Button className="w-full btn-success empurra" size="lg" icon={<Plus className="w-5 h-5 mr-2" />}>
            + Registrar movimentação
          </Button>
        </Link>

        <form onSubmit={handleLogout} className="rodape flex justify-center gap-3 fixed bottom-5 left-1/2 -translate-x-1/2 w-full px-5">
          <button type="submit" className="footer-link">
            <LogOut className="w-4 h-4 inline mr-1" /> Sair
          </button>
          <span aria-hidden="true" className="text-gray-400 self-center">·</span>
          <button type="button" onClick={handleDeleteAccount} className="footer-link text-accent-red">
            <Trash2 className="w-4 h-4 inline mr-1" /> Excluir minha conta
          </button>
        </form>
      </main>
    </div>
  );
}