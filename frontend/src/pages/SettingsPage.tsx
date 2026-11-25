import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useLogout } from '../api/hooks';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import { Alert } from '../ui/Alert';
import { ConfirmDialog } from '../ui/Modal';
import { LogOut, Trash2, Shield, Info } from 'lucide-react';

export function SettingsPage() {
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuth();
  const logoutMutation = useLogout();
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/entrar', { replace: true });
    } catch (err) {
      setError('Erro ao sair. Tente novamente.');
    }
  };

  const handleDeleteAccount = async () => {
    try {
      // TODO: implementar exclusão de conta via API
      alert('Funcionalidade de exclusão de conta em desenvolvimento');
      setShowDeleteConfirm(false);
    } catch (err) {
      setError('Erro ao excluir conta.');
    }
  };

  return (
    <div className="flex-1 flex flex-col">
      <header className="header-with-back p-5 border-b border-gray-100 bg-white sticky top-0 z-10">
        <button onClick={() => navigate(-1)} className="back-button" aria-label="Voltar">
          ←
        </button>
        <h1 className="text-xl font-heading font-bold">Configurações</h1>
      </header>

      <main className="page-container">
        {error && <Alert message={error} onDismiss={() => setError(null)} />}

        <Card padding="lg">
          <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-xl">
            <div className="w-14 h-14 rounded-xl bg-primary-100 flex items-center justify-center">
              <Shield className="w-7 h-7 text-primary-500" />
            </div>
            <div>
              <h2 className="font-heading font-bold text-dark-900">{user?.name || 'Usuário'}</h2>
              <p className="text-sm text-gray-500">{user?.email}</p>
            </div>
          </div>
        </Card>

        <Card padding="none" className="mt-5">
          <div className="p-5 border-b border-gray-100">
            <h3 className="font-heading font-bold text-dark-900">Segurança</h3>
            <p className="text-sm text-gray-500 mt-1">Gerencie sua conta e privacidade</p>
          </div>

          <button
            onClick={handleLogout}
            disabled={logoutMutation.isPending}
            className="w-full flex items-center justify-between p-5 hover:bg-gray-50 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center">
                <LogOut className="w-5 h-5 text-accent-red" />
              </div>
              <div>
                <p className="font-medium text-dark-900">Sair da conta</p>
                <p className="text-sm text-gray-500">Encerrar sessão atual</p>
              </div>
            </div>
            {logoutMutation.isPending && <div className="animate-spin rounded-full h-5 w-5 border-2 border-primary-500 border-t-transparent" />}
          </button>

          <div className="border-t border-gray-100" />

          <button
            onClick={() => setShowDeleteConfirm(true)}
            className="w-full flex items-center justify-between p-5 hover:bg-gray-50 transition-colors text-accent-red"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center">
                <Trash2 className="w-5 h-5 text-accent-red" />
              </div>
              <div>
                <p className="font-medium text-accent-red">Excluir minha conta</p>
                <p className="text-sm text-gray-500">Apagar conta e todos os dados permanentemente</p>
              </div>
            </div>
          </button>
        </Card>

        <Card padding="lg" className="mt-5">
          <h3 className="font-heading font-bold text-dark-900 mb-4">Sobre o CaixaCerto</h3>
          <div className="space-y-3 text-sm text-gray-600">
            <p>Gestão financeira simples para autônomos e microempreendedores.</p>
            <p>Separe o dinheiro do seu negócio do seu dinheiro pessoal e saiba quanto sobrou de verdade.</p>
            <div className="pt-2 border-t border-gray-100">
              <p className="font-medium">Versão 1.0.0 (MVP)</p>
              <p>Projeto da disciplina de Engenharia de Software - UFPB Campus IV</p>
            </div>
          </div>
        </Card>
      </main>

      <ConfirmDialog
        isOpen={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        onConfirm={handleDeleteAccount}
        title="Excluir minha conta"
        message="Isso apaga a sua conta e <b>todas</b> as suas movimentações. Não dá para desfazer."
        confirmText="Sim, excluir tudo"
        cancelText="Cancelar"
        variant="danger"
      />
    </div>
  );
}