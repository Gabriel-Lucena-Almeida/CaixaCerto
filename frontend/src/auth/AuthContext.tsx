import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { useLogin, useRegister, useLogout, useRefreshToken } from '../api/hooks';
import type { User, AuthResponse } from '../api/types';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: { name: string; email: string; password: string; confirmPassword: string; consent: boolean }) => Promise<void>;
  logout: () => Promise<void>;
  refreshAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const loginMutation = useLogin();
  const registerMutation = useRegister();
  const logoutMutation = useLogout();
  const refreshMutation = useRefreshToken();

  // Verifica se tem token salvo ao carregar
  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        try {
          // Tenta buscar dados do usuário (ou decodificar JWT)
          // Por simplicidade, armazenamos user no localStorage também
          const storedUser = localStorage.getItem('user');
          if (storedUser) {
            setUser(JSON.parse(storedUser));
          } else {
            // Se não tem user salvo, faz refresh para validar token
            await refreshMutation.mutateAsync({ refreshToken: localStorage.getItem('refreshToken') || '' });
          }
        } catch {
          localStorage.clear();
        }
      }
      setIsLoading(false);
    };
    initAuth();
  }, [refreshMutation]);

  const login = useCallback(async (email: string, password: string) => {
    const response = await loginMutation.mutateAsync({ email, password });
    const userData = response.data.user;
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
  }, [loginMutation]);

  const register = useCallback(async (data: { name: string; email: string; password: string; confirmPassword: string; consent: boolean }) => {
    const response = await registerMutation.mutateAsync(data);
    const userData = response.data.user;
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
  }, [registerMutation]);

  const logout = useCallback(async () => {
    await logoutMutation.mutateAsync();
    setUser(null);
  }, [logoutMutation]);

  const refreshAuth = useCallback(async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      await refreshMutation.mutateAsync({ refreshToken });
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        setUser(JSON.parse(storedUser));
      }
    }
  }, [refreshMutation]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
        refreshAuth,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return context;
}