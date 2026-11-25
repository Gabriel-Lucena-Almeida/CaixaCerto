import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useLogin } from '../api/hooks';
import { useAuth } from '../auth/AuthContext';
import { Logo } from '../ui/Logo';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Alert } from '../ui/Alert';
import { format } from '../utils/money';

const loginSchema = z.object({
  email: z.string().email('E-mail inválido'),
  password: z.string().min(1, 'Senha é obrigatória'),
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const loginMutation = useLogin();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginForm) => {
    setError(null);
    try {
      await login(data.email, data.password);
      navigate('/', { replace: true });
    } catch (err) {
      setError('E-mail ou senha incorretos.');
    }
  };

  return (
    <div className="flex-1 flex flex-col">
      <div className="flex-1 flex flex-col items-center justify-center px-5 py-10">
        <div className="w-full max-w-[430px]">
          <div className="marca flex items-center gap-3 mb-8">
            <Logo className="w-11 h-11" />
            <span className="nome text-3xl font-heading font-bold text-dark-900">CaixaCerto</span>
          </div>

          <div className="bloco space-y-3 mb-8">
            <h1 className="grande text-4xl font-heading font-bold leading-tight">Saiba quanto sobrou de verdade.</h1>
            <p className="suave text-gray-500">Separe o dinheiro do seu negócio do seu dinheiro pessoal em poucos toques.</p>
          </div>

          {error && <Alert message={error} onDismiss={() => setError(null)} />}

          <form onSubmit={handleSubmit(onSubmit)} className="form space-y-5" noValidate>
            <Input
              {...register('email')}
              type="email"
              label="E-mail"
              placeholder="voce@email.com"
              autoComplete="email"
              error={errors.email?.message}
            />

            <Input
              {...register('password')}
              type="password"
              label="Senha"
              placeholder="Sua senha"
              autoComplete="current-password"
              error={errors.password?.message}
            />

            <Button type="submit" className="w-full" size="lg" loading={loginMutation.isPending || isSubmitting}>
              Entrar
            </Button>
          </form>

          <p className="centro text-center text-sm text-gray-500 mt-6">
            Ainda não tem conta?{' '}
            <Link to="/cadastro" className="btn-link">Criar conta</Link>
          </p>
        </div>
      </div>

      <p className="centro text-center text-xs text-gray-400 px-5 pb-5">
        Seus dados financeiros ficam visíveis apenas para você e você pode excluir a conta quando quiser.
      </p>
    </div>
  );
}