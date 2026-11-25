import { Logo } from './Logo';

export function LoadingPage() {
  return (
    <div className="app-container flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <Logo className="w-16 h-16" />
        <div className="w-8 h-8 border-3 border-primary-500 border-t-transparent rounded-full animate-spin" />
        <p className="text-gray-500">Carregando...</p>
      </div>
    </div>
  );
}

export function LoadingInline({ className = '' }: { className?: string }) {
  return (
    <div className={`flex items-center justify-center gap-2 ${className}`}>
      <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
      <span className="text-gray-500 text-sm">Carregando...</span>
    </div>
  );
}

export function Skeleton({ className = '' }: { className?: string }) {
  return (
    <div className={`skeleton ${className}`} aria-hidden="true" />
  );
}