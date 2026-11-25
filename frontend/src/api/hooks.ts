import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, getErrorMessage } from './client';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RefreshRequest,
  Movement,
  MovementCreateRequest,
  MovementUpdateRequest,
  MovementPageResponse,
  MovementFilter,
  SummaryResponse,
} from './types';

// Auth hooks
export function useLogin() {
  return useMutation({
    mutationFn: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
    onSuccess: (response) => {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
    },
  });
}

export function useRegister() {
  return useMutation({
    mutationFn: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
    onSuccess: (response) => {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
    },
  });
}

export function useRefreshToken() {
  return useMutation({
    mutationFn: (data: RefreshRequest) => api.post<AuthResponse>('/auth/refresh', data),
    onSuccess: (response) => {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => Promise.resolve(),
    onSuccess: () => {
      localStorage.clear();
      queryClient.clear();
    },
  });
}

// Movement hooks
export function useMovements(filter: MovementFilter = {}) {
  return useQuery({
    queryKey: ['movements', filter],
    queryFn: () => api.get<MovementPageResponse>('/movements', { params: filter }).then((r) => r.data),
    placeholderData: (previousData) => previousData,
  });
}

export function useRecentMovements(type: 'NEGOCIO' | 'PESSOAL') {
  return useQuery({
    queryKey: ['movements', 'recent', type],
    queryFn: () => api.get<Movement[]>(`/movements/recent`, { params: { type } }).then((r) => r.data),
  });
}

export function useMovement(id: number) {
  return useQuery({
    queryKey: ['movements', id],
    queryFn: () => api.get<Movement>(`/movements/${id}`).then((r) => r.data),
    enabled: !!id,
  });
}

export function useCreateMovement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: MovementCreateRequest) => api.post<Movement>('/movements', data).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['movements'] });
      queryClient.invalidateQueries({ queryKey: ['summary'] });
    },
  });
}

export function useUpdateMovement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: MovementUpdateRequest }) =>
      api.put<Movement>(`/movements/${id}`, data).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['movements'] });
      queryClient.invalidateQueries({ queryKey: ['summary'] });
    },
  });
}

export function useDeleteMovement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete(`/movements/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['movements'] });
      queryClient.invalidateQueries({ queryKey: ['summary'] });
    },
  });
}

// Summary hook
export function useMonthSummary(month?: string) {
  return useQuery({
    queryKey: ['summary', month],
    queryFn: () => api.get<SummaryResponse>('/movements/summary', { params: { month } }).then((r) => r.data),
  });
}

// Error handling helper
export function handleApiError(error: unknown, setFieldError?: (field: string, message: string) => void): string {
  const message = getErrorMessage(error);
  const field = error instanceof Error && 'response' in error
    ? (error as any).response?.data?.field
    : undefined;

  if (field && setFieldError) {
    setFieldError(field, message);
  }

  return message;
}