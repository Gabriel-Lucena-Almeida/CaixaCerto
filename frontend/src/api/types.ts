// Tipos compartilhados entre frontend e backend

export type MovementNature = 'ENTRADA' | 'SAIDA';
export type MovementType = 'NEGOCIO' | 'PESSOAL';

export interface User {
  id: number;
  name: string;
  email: string;
  firstName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  consent: boolean;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface Movement {
  id: number;
  date: string; // ISO date
  nature: MovementNature;
  type: MovementType;
  category: string;
  amountCents: number;
  formattedValue: string;
  description: string;
  createdAt: string;
}

export interface MovementCreateRequest {
  nature: MovementNature;
  type: MovementType;
  category: string;
  value: string; // Formato string "95,00"
  description: string;
  date: string; // ISO date
}

export interface MovementUpdateRequest {
  nature?: MovementNature;
  type?: MovementType;
  category?: string;
  value?: string;
  description?: string;
  date?: string;
}

export interface MovementPageResponse {
  content: Movement[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface MovementFilter {
  type?: MovementType;
  nature?: MovementNature;
  startDate?: string;
  endDate?: string;
  category?: string;
  page?: number;
  size?: number;
}

export interface SummaryResponse {
  month: string; // "2024-01"
  entradasNegocio: number;
  saidasNegocio: number;
  entradasPessoal: number;
  saidasPessoal: number;
  lucroReal: number;
  saldoPessoal: number;
  sobraDoMes: number;
  formattedEntradasNegocio: string;
  formattedSaidasNegocio: string;
  formattedEntradasPessoal: string;
  formattedSaidasPessoal: string;
  formattedLucroReal: string;
  formattedSaldoPessoal: string;
  formattedSobraDoMes: string;
  chartData: {
    hasData: boolean;
    message?: string;
    sobraPercent: number;
    custosPercent: number;
    pessoalPercent: number;
  };
}

export const DEFAULT_CATEGORIES = [
  'Material',
  'Aluguel',
  'Divulgação',
  'Taxas',
  'Outros',
] as const;

export const DEFAULT_CATEGORY = 'Outros';

export function getCategoryForNature(nature: MovementNature, category: string): string {
  if (nature === 'ENTRADA') return 'Receita';
  return DEFAULT_CATEGORIES.includes(category as any) ? category : DEFAULT_CATEGORY;
}