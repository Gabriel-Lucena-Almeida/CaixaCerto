import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useRegister } from '../api/hooks';
import { useAuth } from '../auth/AuthContext';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Alert } from '../ui/Alert';
import { format } from '../utils/money';

const registerSchema = z.object({
  name: z.string().min(2, 'Informe seu nome (de 2 a 80 caracteres)').max(80, 'Nome muito longo'),
  email: z.string().email('Informe um e-mail válido').max(120, 'E-mail muito longo'),
  password: z.string().min(6, 'A senha precisa ter pelo menos 6 caracteres').max(100, 'Senha muito longa'),
  confirmPassword: z.string(),
  consent: z.literal(true, { errorMap: () => ({ message: 'Para criar a conta, marque que você concorda com o uso dos seus dados.' }) }),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'As duas senhas precisam ser iguais.',
  path: ['confirmPassword'],
});

type RegisterForm = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const { register: authRegister } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      consent: false,
    },
  });

  const onSubmit = async (data: RegisterForm) => {
    setError(null);
    try {
      await authRegister(data.name, data.email, data.password, data.confirmPassword, data.consent);
      navigate('/', { replace: true });
    } catch (err: any) {
      setError(err?.message || 'Erro ao criar conta. Tente novamente.');
    }
  };

  return (
    <div className="flex-1 flex flex-col">
      <header className="header-with-back p-5 border-b border-gray-100 bg-white sticky top-0 z-10">
        <button onClick={() => navigate('/entrar')} className="back-button" aria-label="Voltar">
          ←
        </button>
        <h1 className="text-xl font-heading font-bold">Criar conta</h1>
      </header>

      <main className="page-container">
        <p className="suave text-gray-500">Preencha os dados abaixo para começar a organizar o seu caixa.</p>

        {error && <Alert message={error} onDismiss={() => setError(null)} />}

        <form onSubmit={handleSubmit(onSubmit)} className="form space-y-5" noValidate>
          <Input
            {...register('name')}
            label="Nome"
            placeholder="Como você se chama"
            autoComplete="name"
            maxLength={80}
            error={errors.name?.message}
          />

          <Input
            {...register('email')}
            type="email"
            label="E-mail"
            placeholder="voce@email.com"
            autoComplete="email"
            maxLength={120}
            error={errors.email?.message}
          />

          <Input
            {...register('password')}
            type="password"
            label="Senha"
            placeholder="Pelo menos 6 caracteres"
            autoComplete="new-password"
            minLength={6}
            maxLength={100}
            error={errors.password?.message}
          />

          <Input
            {...register('confirmPassword')}
            type="password"
            label="Confirmar senha"
            placeholder="Repita a senha"
            autoComplete="new-password"
            minLength={6}
            maxLength={100}
            error={errors.confirmPassword?.message}
          />

          <div className="aceite flex gap-3 items-start">
            <input
              {...register('consent')}
              type="checkbox"
              id="consentimento"
              className="w-5 h-5 mt-1 text-primary-500 border-gray-300 rounded focus:ring-primary-500"
              required
            />
            <label htmlFor="consentimento" className="text-sm text-gray-600 leading-relaxed">
              Concordo que o CaixaCerto guarde meu nome, meu e-mail e minhas movimentações para funcionar.
              Posso excluir minha conta e meus dados quando quiser.
            </label>
          </div>
          {errors.consent && (
            <p className="error-message" role="alert">{errors.consent.message}</p>
          )}

          <Button type="submit" className="w-full" size="lg" loading={isSubmitting}>
            Criar conta
          </Button>
        </form>

        <p className="centro text-center text-sm text-gray-500 mt-6">
          Já tem conta? <Link to="/entrar" className="btn-link">Entrar</Link>
        </p>

        <p className="centro text-center text-xs text-gray-400 mt-4">
          Pedimos só o necessário: nome, e-mail e senha. Seus dados financeiros ficam visíveis apenas para você.
        </p>
      </main>
    </div>
  );
}