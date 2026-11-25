import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMonthSummary } from '../api/hooks';
import { format, formatInteger } from '../utils/money';
import { Card } from '../ui/Card';
import { Logo } from '../ui/Logo';
import { Alert } from '../ui/Alert';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';

export function SummaryPage() {
  const [month, setMonth] = useState(() => new Date().toISOString().slice(0, 7)); // YYYY-MM
  const { data: summary, isLoading, refetch } = useMonthSummary(month);
  const [error, setError] = useState<string | null>(null);

  const handleMonthChange = (newMonth: string) => {
    setMonth(newMonth);
    refetch();
  };

  const handlePrevMonth = () => {
    const [year, monthNum] = month.split('-').map(Number);
    const date = new Date(year, monthNum - 2, 1); // monthNum - 2 porque Date usa 0-indexed
    setMonth(date.toISOString().slice(0, 7));
  };

  const handleNextMonth = () => {
    const [year, monthNum] = month.split('-').map(Number);
    const date = new Date(year, monthNum, 1);
    // Não permite ir para o futuro
    if (date > new Date()) return;
    setMonth(date.toISOString().slice(0, 7));
  };

  if (isLoading && !summary) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-3 border-primary-500 border-t-transparent" />
      </div>
    );
  }

  if (!summary) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <Alert message="Não foi possível carregar o resumo." type="error" />
      </div>
    );
  }

  const chartData = summary.chartData;
  const hasChartData = chartData?.hasData;

  // Dados para o gráfico de barras
  const barData = hasChartData ? [
    { name: 'Sobra', value: chartData.sobraPercent, color: '#12805C' },
    { name: 'Custos do negócio', value: chartData.custosPercent, color: '#14263B' },
    { name: 'Gastos pessoais', value: chartData.pessoalPercent, color: '#B8402C' },
  ] : [];

  const monthName = new Date(month + '-01').toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  const capitalizedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1);

  return (
    <div className="flex-1 flex flex-col">
      <header className="header-with-back p-5 border-b border-gray-100 bg-white sticky top-0 z-10">
        <button onClick={handlePrevMonth} className="back-button" aria-label="Mês anterior" disabled={isLoading}>
          ←
        </button>
        <div className="flex-1 text-center">
          <span className="mes text-sm text-gray-500 block">{capitalizedMonth}</span>
          <h1 className="text-xl font-heading font-bold">Resumo do mês</h1>
        </div>
        <button onClick={handleNextMonth} className="w-11" aria-label="Próximo mês" disabled={isLoading} />
      </header>

      <main className="page-container">
        {error && <Alert message={error} onDismiss={() => setError(null)} />}

        <Card variant="green" padding="lg" className="summary-card">
          <span className="rotulo-verde text-primary-700 font-medium">Lucro real do negócio</span>
          <strong className="destaque text-dark-900">{format(summary.lucroReal)}</strong>
          <span className="pequeno text-xs text-gray-600">
            Entradas do negócio menos saídas do negócio. Vale só para o que foi registrado.
          </span>
        </Card>

        <h2 className="text-lg font-heading font-bold">Para onde foi o dinheiro</h2>

        {hasChartData ? (
          <Card padding="lg">
            <div className="h-40">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={barData} layout="vertical" margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E3EAE7" />
                  <XAxis type="number" domain={[0, 100]} tick={false} axisLine={false} />
                  <YAxis type="category" dataKey="name" tick={{ fontSize: 13, fill: '#52636B' }} axisLine={false} width={120} />
                  <Tooltip
                    formatter={(value: number) => [`${value}%`, '']}
                    contentStyle={{
                      backgroundColor: '#fff',
                      border: '1px solid #DCE4E1',
                      borderRadius: '12px',
                      boxShadow: '0 4px 16px rgba(20, 38, 59, 0.12)',
                    }}
                  />
                  <Bar dataKey="value" radius={[0, 8, 8, 0]} barSize={18}>
                    {barData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

            <div className="legenda flex flex-wrap gap-2 mt-4">
              {barData.map((entry) => (
                <span key={entry.name} className="legenda-item flex items-center gap-2 text-sm text-gray-500">
                  <span className="legenda-dot w-3 h-3 rounded-full" style={{ backgroundColor: entry.color }} />
                  <span>{entry.name} {entry.value}%</span>
                </span>
              ))}
            </div>
          </Card>
        ) : (
          <Card padding="lg">
            <div className="empty-state">
              <p>Registre entradas e saídas para ver para onde foi o dinheiro.</p>
            </div>
          </Card>
        )}

        <div className="tabela data-table">
          <div className="data-table-row">
            <dt className="data-table-cell">Entradas do negócio</dt>
            <dd className="data-table-cell text-right amount-positive font-bold">{format(summary.entradasNegocio)}</dd>
          </div>
          <div className="data-table-row">
            <dt className="data-table-cell">Saídas do negócio</dt>
            <dd className="data-table-cell text-right amount-negative font-bold">{format(summary.saidasNegocio)}</dd>
          </div>
          <div className="data-table-row">
            <dt className="data-table-cell">Entradas pessoais</dt>
            <dd className="data-table-cell text-right amount-positive font-bold">{format(summary.entradasPessoal)}</dd>
          </div>
          <div className="data-table-row">
            <dt className="data-table-cell">Gastos pessoais</dt>
            <dd className="data-table-cell text-right amount-negative font-bold">{format(summary.saidasPessoal)}</dd>
          </div>
          <div className="data-table-row bg-gray-50">
            <dt className="data-table-cell data-table-total">Sobra do mês</dt>
            <dd className="data-table-cell text-right data-table-total">{format(summary.sobraDoMes)}</dd>
          </div>
        </div>

        <p className="pequeno text-center text-gray-500 empurra mt-4">
          Dica: separe uma parte do lucro para a sua reserva antes de gastar.
        </p>
      </main>
    </div>
  );
}